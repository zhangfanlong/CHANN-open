import torch
import torch.nn as nn
import torch.nn.functional as F
from src.utils import matrix_mul, element_wise_mul

class EvoAttNet(nn.Module):
    def __init__(self, encode_dim=128, hidden_dim=128, num_classes=2):
        super(EvoAttNet, self).__init__()

        self.mem_weight = nn.Parameter(torch.Tensor(2 * encode_dim, 2 * hidden_dim))
        self.mem_bias = nn.Parameter(torch.zeros(1, 2 * encode_dim))
        self.evo_weight = nn.Parameter(torch.Tensor(2 * hidden_dim, 1))

        self.lstm = nn.LSTM(input_size=2 * encode_dim, hidden_size=hidden_dim, num_layers=1,
                            bidirectional=True)
        self.fc = nn.Linear(2 * hidden_dim, num_classes)
        self.softmax = nn.Softmax()
        self._create_weights(mean=0.0, std=0.05)

    def _create_weights(self, mean=0.0, std=0.05):
        self.mem_weight.data.normal_(mean, std)
        self.evo_weight.data.normal_(mean, std)

    def forward(self, input, hidden_state, c_state):
        f_output, h_output = self.lstm(input, (hidden_state, c_state))
        output = matrix_mul(f_output, self.mem_weight, self.mem_bias)
        output = matrix_mul(output, self.evo_weight).permute(1, 0)
        output = F.softmax(output, dim=1)
        output = element_wise_mul(f_output, output.permute(1, 0)).squeeze(0)
        output = self.fc(output)
        output = self.softmax(output)

        return output, h_output
