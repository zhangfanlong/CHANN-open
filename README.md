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

2. unzip the data.zip to data
   
3. If run with the whole dataset
   ```shell
   python train.py attention=True/False
   ```

4. If run with the specify projects
   ```shell
   python trainCross.py
   ```
