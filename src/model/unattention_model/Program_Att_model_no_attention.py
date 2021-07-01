import torch
import torch.nn as nn
import torch.nn.functional as F
from torch.autograd import Variable

from src.Tree_Encoder import TreeEncoder

class Program_Att_Net(nn.Module):
    def __init__(self, device, embedding_dim, hidden_dim, vocab_size, max_member_length,
                 max_life_length, encode_dim, label_size, batch_size, mem_model,
                 evo_model, use_gpu=True, pretrained_weight=None):
        super(Program_Att_Net, self).__init__()
        self.device = device
        self.hidden_dim = hidden_dim
        self.gpu = use_gpu
        self.batch_size = batch_size
        self.vocab_size = vocab_size
        self.max_member_length = max_member_length
        self.max_life_length = max_life_length
        self.embedding_dim = embedding_dim
        self.encode_dim = encode_dim
        self.label_size = label_size
        self.mem_model = mem_model
        self.evo_model = evo_model

        self.bigru = nn.GRU(self.encode_dim, self.hidden_dim, bidirectional=True, dropout=0.3, batch_first=True)

        self.encoder = TreeEncoder(self.vocab_size, self.embedding_dim, self.encode_dim,
                                   self.batch_size, self.gpu, pretrained_weight)

        from src.model.unattention_model.MemberAttNet_BiGRU_no_attentionpy import MemberAttNet
        self.member_att = MemberAttNet(self.hidden_dim, self.hidden_dim)

        from src.model.unattention_model.EvoAttNet_LSTM_no_attention import EvoAttNet
        self.evo_att = EvoAttNet(self.hidden_dim, self.hidden_dim, self.label_size)

        self._init_hidden_state()

    def _init_hidden_state(self, last_batch_size=None, num_trees=None, num_life=None):
        if last_batch_size:
            batch_size = last_batch_size
        else:
            batch_size = self.batch_size

        if num_trees == None:
            num_trees = self.batch_size

        if num_life == None:
            num_life = self.batch_size

        self.trees_hidden_state = Variable(torch.zeros(2, num_trees, self.hidden_dim))

        self.member_hidden_state = Variable(torch.zeros(2, num_life, self.hidden_dim))

        self.evo_hidden_state = Variable(torch.zeros(2, batch_size, self.hidden_dim))
        self.evo_c_state = Variable(torch.zeros(2, batch_size, self.hidden_dim))

        if torch.cuda.is_available():
            self.trees_hidden_state = self.trees_hidden_state.to(self.device)
            self.member_hidden_state = self.member_hidden_state.to(self.device)
            self.evo_hidden_state = self.evo_hidden_state.to(self.device)

            self.evo_c_state = self.evo_c_state.to(self.device)

    def get_zeros(self, num):
        zeros = Variable(torch.zeros(num))
        if self.gpu:
            return zeros.to(self.device)
        return zeros

    def forward(self, input, member_len, life_len):

        input = [tree for tree_evo in input for gs in tree_evo for tree in gs]
        input_len = len(input)

        lens = [len(item) for item in input]

        max_len = max(lens)

        encodes = []
        for i in range(input_len):
            for j in range(lens[i]):
                encodes.append(input[i][j])

        encodes = self.encoder(encodes, sum(lens))
        seq, start, end = [], 0, 0
        for i in range(input_len):
            end += lens[i]
            if max_len - lens[i]:
                seq.append(self.get_zeros([max_len - lens[i], self.encode_dim]))
            seq.append(encodes[start:end])
            start = end
        encodes = torch.cat(seq)
        encodes = encodes.view(input_len, max_len, -1)

        gru_out, hidden = self.bigru(encodes, self.trees_hidden_state)
        gru_out = torch.transpose(gru_out, 1, 2)
        gru_out = F.max_pool1d(gru_out, gru_out.size(2)).squeeze(2)

        gru_out = gru_out.unsqueeze(1)

        member_input = gru_out.view(-1, self.max_member_length, self.encode_dim * 2)

        member_output = self.member_att(member_input.permute(1, 0, 2), self.member_hidden_state).unsqueeze(1)
        evo_input = member_output.view(-1, self.max_life_length, self.encode_dim * 2)

        evo_output, self.evo_hidden_state = self.evo_att(evo_input.permute(1, 0, 2), self.evo_hidden_state, self.evo_c_state)

        return evo_output
