import tensorflow as tf 
import numpy as np
import os
import regex
import time


# Waver: convert songs written in a string like abc text to .wav files
class Waver:

  def __init__(self, abc_text):
    self.abc_text = abc_text
    self.songs = ''

  def extract_song_snippet(self):
    pattern = '\n\n(.*?)\n\n'
    search_results = regex.findall(pattern, self.abc_text, overlapped=True, flags=regex.DOTALL)
    self.songs = [song for song in search_results]
    print ("Found {} possible songs in generated texts".format(len(self.songs)))

  def save_song_to_abc(self, song, basename):
    full_name = "{}.abc".format(basename)
    with open(full_name, "w") as f:
        f.write(song)
  
  def abc_to_wav(self, basename):
    os.system('abc2midi "{}.abc" -o "{}.mid"'.format(basename, basename))
    os.system('timidity "{}.mid" -Ow "{}.wav"'.format(basename, basename))
    #os.system('rm "{}.abc" "{}.mid"'.format(basename, basename))
    os.system('rm "{}.mid"'.format(basename))
  
  def save_songs(self, dir, num_of_songs):
    print('================\nsave_songs() starts\n================')
    if not os.path.isdir(dir):
      os.mkdir(dir)

    self.extract_song_snippet()

    if len(self.songs) == 0:
        print ("No valid songs found in text.")

    i=0
    for song in self.songs:
        if 'T:' in song:
            pattern = 'T:([\w \']*)\n'
            match = regex.search(pattern, song)
            if match:
              basename=match.group(1).strip()
              # Get the timestamp
              utctime=time.time()
              timestamp=str(time.time()).replace('.','')
              basename=basename+'_'+timestamp+'_' 
              basename=os.path.join(dir, basename)
              self.save_song_to_abc(song, basename)
              self.abc_to_wav(basename)
              print('Trying to save "{}.wav".'.format(basename))
            else:
              print("Title of the song is not valid.")    
        # only save first 10 songs
        i=i+1
        if i==num_of_songs: break

    print('================\nsave_songs() ends\n================')


# Custom LSTM layer
def LSTM(rnn_units): 
  return tf.keras.layers.LSTM(
    rnn_units, 
    return_sequences=True, 
    recurrent_initializer='glorot_uniform',
    recurrent_activation='sigmoid',
    stateful=True,
  )


# Define model
def build_model(vocab_size, embedding_dim, rnn_units, batch_size):
  print('================\nbuild_model() starts\n================')
  model = tf.keras.Sequential([
    # Layer 1: Embedding layer to transform indices into dense vectors of a size `embedding_dim`
    tf.keras.layers.Embedding(vocab_size, embedding_dim, batch_input_shape=[batch_size, None]),

    # Layer 2: LSTM with `rnn_units` number of units. 
    LSTM(rnn_units), 

    # Layer 3: Dense (fully-connected) layer that transforms the LSTM output into the vocabulary size. 
    tf.keras.layers.Dense(vocab_size)
  ])
  print('================\nbuild_model() ends\n================')
  return model


# Function generating abc text using the trained RNN model
def generate_text(model, vocab, start_string, generation_length=3000):
  print('================\ngenerate_text() starts\n================')
  # Creating a mapping from unique characters to indices
  char2idx = {u:i for i, u in enumerate(vocab)}
  # Create a mapping from indices to characters
  idx2char = np.array(vocab)
  # Convert the start string to numbers (vectorize)
  input_eval = [char2idx[s] for s in start_string]
  # Add batch dimension
  input_eval = tf.expand_dims(input_eval, 0)
  # Empty string to store our results
  text_generated = []

  model.reset_states()
  for i in range(generation_length):
      prediction = model(input_eval) 
      # Remove the batch dimension
      prediction = tf.squeeze(prediction, 0)
      predicted_id = tf.random.categorical(prediction, num_samples=1)[-1,0].numpy()
      # Pass the prediction along with the previous hidden state as the next inputs to the model
      input_eval = tf.expand_dims([predicted_id], 0)
      # Add the predicted character to the generated text
      text_generated.append(idx2char[predicted_id])

  print('================\ngenerate_text() ends\n================')   
  return (start_string + ''.join(text_generated))
