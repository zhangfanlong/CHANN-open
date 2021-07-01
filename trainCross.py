from src.utils import *
from src.model.attention_model.Program_Att_model import Program_Att_Net
from src.focalloss import FocalLoss
import argparse
import shutil

import pandas as pd
import torch
import numpy as np
import warnings
import os

import time

warnings.filterwarnings(action='ignore', category=UserWarning, module='gensim')
from gensim.models.word2vec import Word2Vec

os.environ['CUDA_DEVICE_ORDER'] = 'PCI_BUS_ID'
os.environ["CUDA_VISIBLE_DEVICES"] = "1"


def get_args():
    parser = argparse.ArgumentParser(description='add the parameters')
    parser.add_argument("--lr", type=float, default=0.0003, help='the learning rate of the model')
    parser.add_argument("--batch_size", type=int, default=13, help='the batch sizes of the data')
    parser.add_argument("--num_epoches", type=int, default=50, help='the epoches of the train')
    parser.add_argument("--num_classes", type=int, default=2, help='the classes of the data')

    parser.add_argument("--encode_dim", type=int, default=128, help='the encode dimension ')
    parser.add_argument("--hidden_dim", type=int, default=128, help='the hidden dimension')

    parser.add_argument("--es_patience", type=int, default=10, help='the test patience')
    parser.add_argument("--es_min_delta", type=float, default=0.0, help='the test min delta ')
    parser.add_argument("--test_interval", type=int, default=1, help='the test interval')

    parser.add_argument("--alpha", default=2)
    parser.add_argument("--gamma", default=4)

    parser.add_argument("--mode", default=0.6)

    parser.add_argument("--sys_name", type=str, default='dnsjava', help='the name of the clone system')
    parser.add_argument("--mem_model", type=str, default='BiGRU', help='the MemberAttNet model')
    parser.add_argument("--evo_model", type=str, default='LSTM', help='the EvoAttNet model')

    args = parser.parse_args()
    return args

def train(opt):
    use_gpu = torch.cuda.is_available()
    if use_gpu:
        torch.cuda.manual_seed(123)
        device = torch.device('cuda:0')
    else:
        torch.manual_seed(123)
        device = torch.device('cpu')

    saved_path = f'trained/trained_{opt.sys_name}_models/{opt.mem_model}/{opt.evo_model}'
    os.makedirs(saved_path, exist_ok=True)
    output_file = open(saved_path + os.sep + 'logs.txt', 'w')
    output_file.write("Model's parameters: {}".format(vars(opt)))

    train_set_path = f'data/{opt.sys_name}/train/blocks.pkl'
    test_set_path = f'data/{opt.sys_name}/test/blocks.pkl'
    train_set = pd.read_pickle(train_set_path)
    test_set = pd.read_pickle(test_set_path)

    print('get max lengths...')
    # max_member_length, max_life_length = get_max_lengths(train_set)
    max_member_length, max_life_length = 4, 5

    print('get trees preprocess...')
    train_set = trees_preprocess(train_set, max_member_length, max_life_length)
    test_set = trees_preprocess(test_set, max_member_length, max_life_length)

    word2vec_path = f'data_def2_cross/all_cross/train/embedding/node_w2v_128'
    word2vec = Word2Vec.load(word2vec_path).wv
    embeddings = np.zeros((word2vec.syn0.shape[0] + 1, word2vec.syn0.shape[1]), dtype="float32")
    embeddings[:word2vec.syn0.shape[0]] = word2vec.syn0
    max_tokens = word2vec.syn0.shape[0]
    embedding_dim = word2vec.syn0.shape[1]

    men_model = opt.mem_model
    evo_model = opt.evo_model
    model = Program_Att_Net(device, embedding_dim, opt.hidden_dim, max_tokens + 1, max_member_length,
                            max_life_length, opt.encode_dim, opt.num_classes, opt.batch_size,
                            use_gpu=use_gpu, pretrained_weight=None, mem_model=men_model,
                            evo_model=evo_model)

    if use_gpu:
        model.to(device)

    criterion = FocalLoss(opt.gamma, opt.alpha)
    optimizer = torch.optim.Adam(filter(lambda p: p.requires_grad, model.parameters()), lr=opt.lr)
    best_loss = 1e5
    best_epoch = 0
    num_iter_per_epoch = len(train_set) // opt.batch_size + 1
    for epoch in range(opt.num_epoches):
        train_loader = get_batch(train_set, opt.batch_size, is_shuffle=False)
        for batch_idx, (train_inputs, labels, member_len, life_len) in enumerate(train_loader):
            if use_gpu:
                labels = labels.to(device)
            optimizer.zero_grad()
            member_len_sum = sum([j for i in member_len for j in i])
            model._init_hidden_state(len(labels), member_len_sum, sum(life_len))
            predictions = model(train_inputs, member_len, life_len)
            loss = criterion(predictions, labels)
            loss.backward()
            optimizer.step()
            training_metrics = get_evaluation(labels.cpu().numpy(), predictions.cpu().detach().numpy(),
                                              list_metrics=["accuracy", "precision", "f1", "recall"], mode=opt.mode)
            print(
                "Epoch: {}/{}, Iteration: {}/{}, Lr: {}, Loss: {}, Accuracy: {}, Precision: {}, Recall: {}, F1: {}".format(
                    epoch + 1,
                    opt.num_epoches,
                    batch_idx + 1,
                    num_iter_per_epoch,
                    optimizer.param_groups[0]['lr'],
                    loss,
                    training_metrics["accuracy"],
                    training_metrics["precision"],
                    training_metrics["recall"],
                    training_metrics["f1"],
                ))

        if epoch % opt.test_interval == 0:
            model.eval()
            loss_ls = []
            te_label_ls = []
            te_pred_ls = []
            test_loader = get_batch(test_set, opt.batch_size)
            for test_input, te_labels, te_member_len, te_life_len in test_loader:
                num_sample = len(te_labels)
                if use_gpu:
                    te_labels = te_labels.to(device)
                with torch.no_grad():
                    te_member_len_sum = sum([j for i in te_member_len for j in i])
                    # print(f'test:te_member_len_sum:{te_member_len_sum}')
                    model._init_hidden_state(num_sample, te_member_len_sum, sum(te_life_len))
                    te_predictions = model(test_input, te_member_len, te_life_len)
                te_loss = criterion(te_predictions, te_labels)
                loss_ls.append(te_loss * num_sample)
                te_label_ls.extend(te_labels.clone().cpu())
                te_pred_ls.append(te_predictions.clone().cpu())
            te_loss = sum(loss_ls) / len(test_set)
            te_pred = torch.cat(te_pred_ls, 0)
            te_label = np.array(te_label_ls)
            test_metrics = get_evaluation(te_label, te_pred.numpy(),
                                          list_metrics=["accuracy", "precision", "recall", "f1", "confusion_matrix"],
                                          mode=opt.mode)
            output_file.write(
                "Epoch: {}/{} \nTest loss: {} Test accuracy: {} Test precision: {} Test recall: {} Test f1: {}\nTest confusion matrix: \n{}\n\n".format(
                    epoch + 1, opt.num_epoches,
                    te_loss,
                    test_metrics["accuracy"],
                    test_metrics["precision"],
                    test_metrics["recall"],
                    test_metrics["f1"],
                    test_metrics["confusion_matrix"]))
            print("Epoch: {}/{}, Lr: {}, Loss: {}, Accuracy: {}, Precision: {}, Recall: {}, F1: {}".format(
                epoch + 1,
                opt.num_epoches,
                optimizer.param_groups[0]['lr'],
                te_loss,
                test_metrics["accuracy"],
                test_metrics["precision"],
                test_metrics["recall"],
                test_metrics["f1"],
            ))
            model.train(True)

            if te_loss + opt.es_min_delta <= best_loss:
                best_loss = te_loss
                best_epoch = epoch
                torch.save(model.state_dict(), saved_path + os.sep + "whole_model_han.pth")

            # Early stopping
            if epoch - best_epoch > opt.es_patience > 0:
                print("Stop training at epoch {}. The lowest loss achieved is {}".format(best_epoch + 1, best_loss))
                break

    del model, optimizer, train_loader, test_loader
    torch.cuda.empty_cache()

if __name__ == '__main__':
    try:
        file_name = ['dnsjava', 'tuxguitar', 'jfreechart', 'jabref', 'jedit', 'freecol', 'mina', 'argouml']
        for i in file_name:
            print(f'{i} start trainning...')

            start_time = time.time()
            opt = get_args()
            opt.sys_name = i
            train(opt)
            end_time = time.time()
            cost_time = int(end_time - start_time)
            m, s = divmod(cost_time, 60)
            h, m = divmod(m, 60)
            time_str = 'spent time ：' + str(h) + 'h' + str(m) + 'm' + str(s) + 's'
            print(time_str)
            print('-------------------------------------------------------------------')

    except Exception as e:
        print(str(e))
