import torch.nn as nn
import torch.nn.functional as F

class MemberAttNet(nn.Module):
    def __init__(self, encode_dim=128, hidden_dim=128):
        super(MemberAttNet, self).__init__()

        self.gru = nn.GRU(2 * encode_dim, hidden_dim, bidirectional=True)

    def forward(self, input, hidden_state):

        f_output, h_output = self.gru(input, hidden_state)
        f_output = F.max_pool1d(f_output.permute(1, 2, 0), f_output.size(0)).permute(0, 2, 1).squeeze(1)

        return f_output