from torch.utils.data import Dataset

class MyDataset(Dataset):
    def __init__(self, data_tree, dict_path):
        super(MyDataset, self).__init__()

        codes, labels = [], []

        for idx, (trees, label, evolen) in data_tree.iterrows():
            codes.append(trees)
            labels.append(int(label))

        self.codes = codes
        self.labels = labels
        self.evolen = evolen

        self.num_classes = len(set(self.labels))

    def __len__(self):
        return len(self.labels)

    def __getitem__(self, index):
        trees = self.codes[index]
        label = self.labels[index]

        if len(trees) > self.max_length_data * 2:
            trees = self.codes[:self.max_length_data]

        return trees, label