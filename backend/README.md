# Build an Android App Using Firebase and the App Engine (Flexible Environment)


## Google App Engine Flexible Environment
* Install python
* Install docker
* Install Google Cloud SDK
* Create a Firebase project (Google Cloud project)
* Generate service account key
* Enable billing https://console.cloud.google.com/
* Create a Firebase realtime database ([Index your data](https://firebase.google.com/docs/database/security/indexing-data))
* Create a Firebase cloud storage (Google Cloud storage)
* Make Google cloud storage public https://console.cloud.google.com/
* Write a python/flask/gunicore backend app 
* Run locally backend app (with Docker)
* Deploy backend app in Google app engine flexible environment


## Backend app
```python
	# Fetch the service account key JSON file contents
	cred = credentials.Certificate('musico-f7f83-firebase-adminsdk-95dmc-ade7d61b2b.json')
	# Initialize the app with a service account, granting admin privileges
	firebase_admin.initialize_app(cred, {
    	'databaseURL': 'https://musico-f7f83.firebaseio.com',
    	'storageBucket': 'musico-f7f83.appspot.com'
	})
```


## Running locally
    $ FLASK_DEBUG=1 flask run --no-reload


## Running locally with Docker
	$ docker build -t musico .
	$ docker run -p 8080:8080 musico


## Deploy to App Engine Flexible Environment
	$ gcloud config set project musico-f7f83
	$ gcloud app deploy




