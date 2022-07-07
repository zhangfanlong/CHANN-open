import pandas as pd
import os

class Pipeline:
    def __init__(self, ratio, root):
        self.ratio = ratio
        self.root = root
        self.sources = None
        self.data_file_path = None
        self.train_file_path = None
        self.dev_file_path = None
        self.test_file_path = None
        self.size = None

    def parse_source(self, output_file, option):
        path = self.root + output_file
        if os.path.exists(path) and option is 'existing':
            source = pd.read_pickle(path)
        else:
            import javalang
            def parse_program(evos):
                evolution = []
                for gp in evos:
                    codes = []
                    for code in gp:
                        if ' enum =' in code or 'Iterator enum' in code or code.find('java.util.Enumeration') != -1:
                        # if code.find('java.util.Enumeration') != -1:
                            code = code.replace('enum', 'enums')
                        tokens = javalang.tokenizer.tokenize(code)
                        parser = javalang.parser.Parser(tokens)

                        # try:
                        #     tree = parser.parse_member_declaration()
                        # except:
                        #     print('aaa')

                        tree = parser.parse_member_declaration()
                        codes.append(tree)
                    evolution.append(codes)
                return evolution

            source = pd.read_pickle(r'data_pkl_def2/data_pkl/tuxguitar.pkl')
            source['evolution'] = source['evolution'].apply(parse_program)
            source.to_pickle(path)
        self.sources = source
        return source

    def split_data(self):
        data = self.sources
        data_num = len(data)
        ratios = [int(r) for r in self.ratio.split(':')]
        train_split = int(ratios[0] / sum(ratios) * data_num)
        # data = data.sample(frac=1, random_state=666)
        train = data.iloc[: train_split]
        test = data.iloc[train_split:]

        def check_or_create(path):
            if not os.path.exists(path):
                os.mkdir(path)

        data_path = self.root
        self.data_file_path = data_path + 'data_.pkl'
        data.to_pickle(self.data_file_path)

        train_path = self.root + 'train/'
        check_or_create(train_path)
        self.train_file_path = train_path + 'train_.pkl'
        train.to_pickle(self.train_file_path)

        test_path = self.root + 'test/'
        check_or_create(test_path)
        self.test_file_path = test_path + 'test_.pkl'
        test.to_pickle(self.test_file_path)

    def dictionary_and_embedding(self, input_file, size):
        self.size = size
        if not input_file:
            input_file = self.train_file_path
        trees = pd.read_pickle(input_file)
        # if not os.path.exists(self.root + 'train/embedding'):
        #     os.mkdir(self.root + 'train/embedding')
        from ..src.utils import get_sequence as func

        def trans_to_sequence(evos):
            sequences = []
            for gp in evos:
                for ast in gp:
                    sequence = []
                    func(ast, sequence)
                    sequences.append(sequence)
            return sequences

        corpus = trees['evolution'].apply(trans_to_sequence)

        codes_corpus = []
        for codes in corpus:
            str_corpus = [' '.join(c) for c in codes]
            codes_corpus.append(str_corpus)
        trees['evolution'] = pd.Series(codes_corpus).values

        trees.to_pickle(self.root + 'train/programs_ns.pkl')

        codes_list_corpus = []
        for list_corpus in corpus:
            for _ in list_corpus:
                codes_list_corpus.append(_)
        from gensim.models.word2vec import Word2Vec
        w2v = Word2Vec(codes_list_corpus, size=size, workers=16, sg=1, min_count=3)
        w2v.save(self.root + 'train/embedding/node_w2v_' + str(size))

    def generate_block_seqs(self, data_path, part):
        from src.utils import get_blocks_v1 as func
        from gensim.models.word2vec import Word2Vec

        word2vec = Word2Vec.load(self.root + 'train/embedding/node_w2v_' + str(self.size)).wv
        vocab = word2vec.vocab
        max_token = word2vec.syn0.shape[0]

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
                    func(code, blocks)
                    tree = []
                    for b in blocks:
                        btree = tree_to_index(b)
                        tree.append(btree)
                    trees.append(tree)
                evolution.append(trees)
            return evolution

        trees = pd.read_pickle(data_path)
        trees['evolution'] = trees['evolution'].apply(trans2seq)
        trees.to_pickle(self.root + part + '/blocks.pkl')

    def run(self):
        print('parse source code...')
        self.parse_source(output_file='ast.pkl', option=None)
        print('split data...')
        # self.split_data()
        print('train word embedding...')
        # self.dictionary_and_embedding(None, 128)
        # self.dictionary_and_embedding(r'I:\project\CHANN_DIR\CCP_DL\data\tuxguitar_all\train\train_.pkl', 128)
        print('generate block sequences...')
        self.generate_block_seqs(self.train_file_path, 'train')
        self.generate_block_seqs(self.test_file_path, 'test')


ppl = Pipeline('9:1', 'data_blocks/')
ppl.run()
