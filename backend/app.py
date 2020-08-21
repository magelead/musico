# Copyright 2015 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# [START gae_flex_storage_app]
import logging
import os
import glob
import time
import regex

from flask import Flask, request, render_template

from musician import Waver
from musician import build_model
from musician import generate_text

import firebase_admin
from firebase_admin import credentials
from firebase_admin import db
from firebase_admin import storage

# Fetch the service account key JSON file contents
cred = credentials.Certificate('firebase.json')
# Initialize the app with a service account, granting admin privileges
firebase_admin.initialize_app(cred, {
    'databaseURL': 'https://musico-f7f83.firebaseio.com',
    'storageBucket': 'musico-f7f83.appspot.com'
})

# Function that writes into firebase realtime database 
def firebase_rd_write(paths, urls, child):
    # Get the timestamp
    utctime = time.gmtime()
    timestamp = '({} {}, {}:{})'.format(str(utctime[1]), 
    	str(utctime[2]), str(utctime[3]), str(utctime[4]))
    # Get the reference to the child node (/child) in database 
    ref = db.reference(child)
    # Add data to the child node
    for path, url in zip(paths, urls):
        name = path.replace('generated/', '').replace('.', ' ')
        name = regex.sub('_.*_','',name)
        ref.push().update({
            "timestamp": timestamp,
            "name": name,
            "url": url,
            "like": 0,
            "dislike": 0
            })


# Function that reads from firebase realtime database 
def firebase_rd_read(child):
    # Get the reference to the child node (/child)
    ref = db.reference(child)
    # Read data from the child node, snapshot is a dictionary
    snapshot = ref.order_by_key().get()
    # Format data
    output=list(snapshot.items())
    output.sort(reverse=True) 
    return output


app = Flask(__name__)


@app.route('/')
def index():
    output = firebase_rd_read('music')
    return render_template('index.html', output=output)

@app.route('/how-it-works')
def how():
    return render_template('how-it-works.html')


@app.route('/musician')
def musician():
    print('================\nmusician() starts\n================')
    # AI generates music 
    # Load dataset
    text = open('data.abc').read()
    # Find the unique characters in the dataset
    vocab = sorted(set(text))
    # Build a simple model with default hyperparameters and pre-trained weights.
    model = build_model(len(vocab), embedding_dim=256, rnn_units=1024, batch_size=1)
    model.load_weights('./ckpt_99.h5')
    # Generate abc text
    text = generate_text(model, vocab, start_string="Xxxx")
    # Instantiate a waver to genearate songs from text
    Waver(text).save_songs('generated',20)

    response = upload()
    
    print('================\nmusician() ends\n================')
    return 'AI musician has finished creating music. <a href="/">Go Back</a><br/><br/>' + response, 200, {'Content-Type': 'text/html; charset=utf-8'}


def upload():
    print('================\nupload() starts\n================')
    # Get the generated file's paths
    paths = glob.glob('generated/*.*')
    # Get the cloud stirage bucket that the file will be uploaded to.
    bucket = storage.bucket()    
    # Create a new blob and upload the file's content to cloud storage.
    urls=[]
    # This loop takes time
    for path in paths:
        blob = bucket.blob(path)
        try:
            blob.upload_from_filename(path)
            urls.append(blob.public_url)
            # Delete the local file at path
            os.system('rm "{}"'.format(path))
        except:
            print("No such file, {}.".format(path))

    # Store music into firebase realtime database
    paths_abc = [ path for path in paths if 'abc' in path]
    urls_abc = [ url for url in urls if 'abc' in url]
    firebase_rd_write(paths_abc, urls_abc, 'abc')
    
    paths_wav = [ path for path in paths if 'wav' in path]
    urls_wav = [ url for url in urls if 'wav' in url]
    firebase_rd_write(paths_wav, urls_wav, 'music')
    
    print('================\nupload() ends\n================')
    if len(urls)==0:
        response = 'No file can be uploaded.'
    else:
        response = '<br/><br/>'.join(urls)
    return response


@app.errorhandler(500)
def server_error(e):
    logging.exception('An error occurred during a request.')
    return """
    An internal error occurred: <pre>{}</pre>
    See logs for full stacktrace.
    """.format(e), 500

