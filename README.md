# CHANN
## Requirements
* javalang 0.13.0
* pytorch 1.4.0+
* gensim 3.8.3
* numpy 1.19.2
* pandas 1.1.2

## How to run
1. Create a virtual environment, and install requirements;
   ```shell
   python3 -m venv env
   source env/bin/activate
   pip install javalang
   ...
   ```

2. Dataset
   1)Download our data from the goole drive: https://drive.google.com/file/d/1ytPrCtHksLY9hgEggJw18Eg3vreWJucv/view?usp=sharing
                                 or dropbox: https://www.dropbox.com/s/8rutbixd9xi2q65/data.zip?dl=0
   
   2)unzip the data.zip to data folder.
   
3. Run with the whole dataset(our effectiveness experiment)
   ```shell
   python train.py attention=True/False
   ```

4. Run with the specify projects(specific-project experiemnt)
   ```shell
   python trainCross.py
   ```
