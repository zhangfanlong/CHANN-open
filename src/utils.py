import pandas as pd
import random
import numpy as np
import torch
from sklearn import metrics
from javalang.ast import Node
from src.tree import BlockNode
import sys
import copy

sys.setrecursionlimit(10000)


def trees_preprocess(data_set, max_member_length, max_life_length):

    def trees_truncation_completion(evo):
        if evo['life_len'] > max_life_length:
            evo['evolution'] = evo['evolution'][(evo['life_len'] - max_life_length):]
            evo['member_len'] = evo['member_len'][(evo['life_len'] - max_life_length):]
            evo['life_len'] = max_life_length
        elif evo['life_len'] < max_life_length:
            extend_len = max_life_length - evo['life_len']
            extend_data = [copy.deepcopy(evo['evolution'][-1]) for _ in range(extend_len)]
            evo['evolution'].extend(extend_data)
            extend_member_len = [evo['member_len'][-1] for _ in range(extend_len)]
            evo['member_len'].extend(extend_member_len)
            evo['life_len'] = max_life_length

        for idx, member_len in enumerate(evo['member_len']):

            if member_len > max_member_length:
                random_idx = random.sample(range(0, member_len), max_member_length)
                evo['evolution'][idx] = [evo['evolution'][idx][i] for i in random_idx]
                evo['member_len'][idx] = max_member_length
            elif member_len < max_member_length:
                random_idx = np.random.randint(member_len, size=(max_member_length - member_len))
                extend_data = [copy.deepcopy(evo['evolution'][idx][i]) for i in random_idx]
                evo['evolution'][idx].extend(extend_data)
                evo['member_len'][idx] = max_member_length

        return evo

    data_set = data_set.apply(trees_truncation_completion, axis=1)

    return data_set


def get_batch(data_set, batch_size, is_shuffle=True):
    if is_shuffle:
        data_set = data_set.sample(frac=1)
    batch_i = 0
    data_len = len(data_set)
    while batch_i * batch_size <= data_len:
        st = batch_i * batch_size
        ed = st + batch_size
        evolutions = list(data_set['evolution'].iloc[st:ed])
        labels = list(data_set['label'].iloc[st:ed])
        member_len = list(data_set['member_len'].iloc[st:ed])
        life_len = list(data_set['life_len'].iloc[st:ed])
        batch_i += 1

        yield evolutions, torch.LongTensor(labels), member_len, life_len


def get_max_lengths(datas):
    member_length_list, life_length_list = [], []

    for idx, (trees, label, member_num, life_num) in datas.iterrows():
        for members in member_num:
            member_length_list.append(members)
        life_length_list.append(life_num)

    sort_member_length = sorted(member_length_list)
    sort_life_length = sorted(life_length_list)

    return sort_member_length[int(0.8 * len(sort_member_length))], sort_life_length[int(0.8 * len(sort_life_length))]


def get_evaluation(y_true, y_prob, list_metrics, mode):
    if mode == 0.5:
        y_pred = np.argmax(y_prob, -1)
    else:
        y_pred = (y_prob[:, 1] > mode).astype(int)
    output = {}
    if 'accuracy' in list_metrics:
        output['accuracy'] = metrics.accuracy_score(y_true, y_pred)
    if 'loss' in list_metrics:
        try:
            output['loss'] = metrics.log_loss(y_true, y_prob)
        except ValueError:
            output['loss'] = -1
    if 'confusion_matrix' in list_metrics:
        output['confusion_matrix'] = str(metrics.confusion_matrix(y_true, y_pred))
    if 'precision' in list_metrics:
        try:
            output['precision'] = metrics.precision_score(y_true, y_pred)
        except ValueError:
            output['precision'] = -1
    if 'recall' in list_metrics:
        try:
            output['recall'] = metrics.recall_score(y_true, y_pred)
        except ValueError:
            output['recall'] = -1
    if 'f1' in list_metrics:
        try:
            output['f1'] = metrics.f1_score(y_true, y_pred)
        except ValueError:
            output['f1'] = -1
    if 'confusion_matrix' in list_metrics:
        output['confusion_matrix'] = str(metrics.confusion_matrix(y_true, y_pred))
    return output

def matrix_mul(input, weight, bias=False):
    feature_list = []
    for feature in input:
        feature = torch.mm(feature, weight)
        if isinstance(bias, torch.nn.parameter.Parameter):
            feature = feature + bias.expand(feature.size()[0], bias.size()[1])
        feature = torch.tanh(feature).unsqueeze(0)
        feature_list.append(feature)

    return torch.cat(feature_list, 0).squeeze(2)
    # return torch.cat(feature_list, 0)


def element_wise_mul(input1, input2):
    feature_list = []
    for feature_1, feature_2 in zip(input1, input2):
        feature_2 = feature_2.unsqueeze(1).expand_as(feature_1)
        feature = feature_1 * feature_2
        feature_list.append(feature.unsqueeze(0))
    output = torch.cat(feature_list, 0)
    # a = torch.sum(output, 1).unsqueeze(0)

    return torch.sum(output, 0).unsqueeze(0)

def get_token(node):
    token = ''
    if isinstance(node, str):
        token = node
    elif isinstance(node, set):
        token = 'Modifier'
    elif isinstance(node, Node):
        token = node.__class__.__name__

    return token


def get_children(root):
    if isinstance(root, Node):
        children = root.children
    elif isinstance(root, set):
        children = list(root)
    else:
        children = []

    def expand(nested_list):
        for item in nested_list:
            if isinstance(item, list):
                for sub_item in expand(item):
                    yield sub_item
            elif item:
                yield item

    return list(expand(children))


def get_sequence(node, sequence):
    token, children = get_token(node), get_children(node)
    sequence.append(token)

    for child in children:
        get_sequence(child, sequence)

    if token in ['ForStatement', 'WhileStatement', 'DoStatement', 'SwitchStatement', 'IfStatement']:
        sequence.append('End')


def get_blocks_v1(node, block_seq):
    name, children = get_token(node), get_children(node)
    logic = ['SwitchStatement', 'IfStatement', 'ForStatement', 'WhileStatement', 'DoStatement']
    if name in ['MethodDeclaration', 'ConstructorDeclaration']:
        block_seq.append(BlockNode(node))
        body = node.body
        for child in body:
            if get_token(child) not in logic and not hasattr(child, 'block'):
                block_seq.append(BlockNode(child))
            else:
                get_blocks_v1(child, block_seq)
    elif name in logic:
        block_seq.append(BlockNode(node))
        for child in children[1:]:
            token = get_token(child)
            if not hasattr(node, 'block') and token not in logic + ['BlockStatement']:
                block_seq.append(BlockNode(child))
            else:
                get_blocks_v1(child, block_seq)
            block_seq.append(BlockNode('End'))
    elif name is 'BlockStatement' or hasattr(node, 'block'):
        block_seq.append(BlockNode(name))
        for child in children:
            if get_token(child) not in logic:
                block_seq.append(BlockNode(child))
            else:
                get_blocks_v1(child, block_seq)
    else:
        for child in children:
            get_blocks_v1(child, block_seq)


class tree_kfold():
    def __init__(self, data_set, kfold):
        self.data_set = data_set
        self.kfold = kfold
        self.interval = len(data_set) // kfold

    def get_kfold(self, i):
        if i == 0:
            test_st = i * self.interval
            test_ed = (i + 1) * self.interval
            test_set = self.data_set.iloc[test_st:test_ed]
            train_set = self.data_set.iloc[test_ed:]
        elif i == (self.kfold - 1):
            test_st = i * self.interval
            test_set = self.data_set.iloc[test_st:]
            train_set = self.data_set.iloc[:test_st]
        else:
            test_st = i * self.interval
            test_ed = (i + 1) * self.interval
            test_set = self.data_set.iloc[test_st:test_ed]
            train_set = pd.concat([self.data_set.iloc[0:test_st], self.data_set.iloc[test_ed:]])
        return train_set, test_set


def dictionary_and_embedding_and_generate_block_seqs(train_set, test_set, size):
    # if not os.path.exists(root + 'train/embedding'):
    #     os.mkdir(root + 'train/embedding')

    def trans_to_sequence(evos):
        sequences = []
        for gp in evos:
            for ast in gp:
                sequence = []
                get_sequence(ast, sequence)
                sequences.append(sequence)
        return sequences

    corpus = train_set['evolution'].apply(trans_to_sequence)
    codes_corpus = []
    for codes in corpus:
        str_corpus = [' '.join(c) for c in codes]
        codes_corpus.append(str_corpus)
    train_set['evolution'] = pd.Series(codes_corpus).values

    codes_list_corpus = []
    for list_corpus in corpus:
        for _ in list_corpus:
            codes_list_corpus.append(_)
    from gensim.models.word2vec import Word2Vec
    w2v = Word2Vec(codes_list_corpus, size=size, workers=16, sg=1, min_count=3)
    w2v = w2v.wv
    # w2v.save(root + 'train/embedding/node_w2v_' + str(size))

    vocab = w2v.vocab
    max_token = w2v.syn0.shape[0]

    def tree_to_index(node):
        token = node.token
        result = [vocab[token].index if token in vocab else max_token]
        children = node.children
        for child in children:
            result.append(tree_to_index(child))
        return result

    def trans2seq(evos):
        evolution = []
        for gp in evos:
            trees = []
            for code in gp:
                blocks = []
                get_blocks_v1(code, blocks)
                tree = []
                for b in blocks:
                    btree = tree_to_index(b)
                    tree.append(btree)
                trees.append(tree)
            evolution.append(trees)
        return evolution

    train_set['evolution'] = train_set['evolution'].apply(trans2seq)
    test_set['evolution'] = test_set['evolution'].apply(trans2seq)

    return train_set, test_set, w2v

