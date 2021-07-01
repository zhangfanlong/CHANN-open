import torch.nn as nn
import torch.nn.functional as F

class EvoAttNet(nn.Module):
    def __init__(self, encode_dim=128, hidden_dim=128, num_classes=2):
        super(EvoAttNet, self).__init__()
        self.lstm = nn.LSTM(2 * encode_dim, hidden_size=hidden_dim, num_layers=1,
                            bidirectional=True)
        self.fc = nn.Linear(2 * hidden_dim, num_classes)
        self.softmax = nn.Softmax()

    def forward(self, input, hidden_state, c_state):
        f_output, h_output = self.lstm(input, (hidden_state, c_state))
        f_output = F.max_pool1d(f_output.permute(1, 2, 0), f_output.size(0)).permute(0, 2, 1).squeeze(1)
        output = self.fc(f_output)
        output = self.softmax(output)

        return output, h_output
