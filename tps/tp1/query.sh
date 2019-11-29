#!bin/sh

curl --get 'https://api.twitter.com/1.1/statuses/user_timeline.json' \
     --data 'count=2&screen_name=twitterapi' \
     --header 'Authorization: OAuth oauth_consumer_key="bfrzljA9EmLC7uZjb5Qt6lGq9", oauth_nonce="cZ7juq8ZFjVJMTxmLqzXONOqV3V3jBcHGk5StasWzARKZnQlWh", oauth_signature="MlgSOQcEBaKA1mh0NjlMCCpsVIqws80C2WkRwFX99XA0L", oauth_signature_method="HMAC-SHA1", oauth_timestamp="1574432352", oauth_token="721323229470703616-IrZXRhyoG1FwKUogN4q0l8J6j1xc8F1", oauth_version="1.1"'
