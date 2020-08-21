FROM gcr.io/google-appengine/python
LABEL python_version=python3.6
RUN virtualenv --no-download /env -p python3.6

EXPOSE 8080

# Set virtualenv environment variables. This is equivalent to running
# source /env/bin/activate
ENV VIRTUAL_ENV /env
ENV PATH /env/bin:$PATH
ADD requirements.txt /app/
RUN pip install --upgrade pip
RUN pip install -r requirements.txt

# Install dependencies
RUN apt-get clean && apt-get update
RUN apt-get install -y abcmidi timidity

ADD . /app/
CMD exec gunicorn -b :$PORT app:app --timeout 900