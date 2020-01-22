# usage: python v2s.py ./v_data/exo ./dataset-temp/
# Don't use / at the end of dataset path
from os import listdir
from os.path import isfile, join
from collections import defaultdict
from natsort import natsorted, ns
import sys

dataset_path = sys.argv[1]
files = [join(dataset_path, f) for f in listdir(dataset_path) if isfile(join(dataset_path, f))]
files = natsorted(files, key=lambda y: y.lower())
n_revisions = len(files)
# Initialize dict entrances with array of zeros
weight_dict = defaultdict(lambda: [0] * n_revisions)

# Put all item names into a dictionary
for revision, filename in enumerate(files):
    text = open(filename, "r")
    for line in text:
        if (line != 'id,weight\n'):
            line = 'root/' + line
            id, weight = line.split(',')
            weight = float(weight)
            # Add weight value to children and parents
            split = id.split('/')
            if len(split) > 1:
                for i in range(len(split)):
                    partial_id = '/'.join(split[:i+1])
                    weight_dict[partial_id][revision] = weight_dict[partial_id][revision] + weight
    text.close()

file_name = dataset_path[dataset_path.rfind('/')+1:]
if file_name == '':
    file_name = 'data'

output_dir = sys.argv[2]
write_file = open(output_dir + '/' + file_name + '.data', 'w')
print(file_name + '.data' + ' written.')

for key in sorted(weight_dict):
    if key != 'root':
        # Child and direct parent
        write_file.write(key + ',' + key[:key.rfind('/')])
        for value in weight_dict[key]:
            write_file.write(',' + str(value))
        write_file.write('\n')
