import torch
import torch.nn as nn
import torch.nn.functional as F
from src.utils import matrix_mul, element_wise_mul

class MemberAttNet(nn.Module):
    def __init__(self, encode_dim=128, hidden_dim=128):
        super(MemberAttNet, self).__init__()

        self.code_weight = nn.Parameter(torch.Tensor(2 * encode_dim, 2 * hidden_dim))
        self.code_bias = nn.Parameter(torch.zeros(1, 2 * encode_dim))
        self.mem_weight = nn.Parameter(torch.Tensor(2 * hidden_dim, 1))

        self.gru = nn.GRU(2 * encode_dim, hidden_dim, bidirectional=True)
        self._create_weights(mean=0.0, std=0.05)

    def _create_weights(self, mean=0.0, std=0.05):
        self.code_weight.data.normal_(mean, std)
        self.mem_weight.data.normal_(mean, std)

    def forward(self, input, hidden_state):

        f_output, h_output = self.gru(input, hidden_state)
        output = matrix_mul(f_output, self.code_weight, self.code_bias)
        output = matrix_mul(output, self.mem_weight).permute(1, 0)
        output = F.softmax(output, dim=1)
        output = element_wise_mul(input, output.permute(1, 0)).squeeze(0)

        return output