import sys
import json
import re


"""
Chriss Santi
Amen Amegnonan

Schema minimal d'un tweet
tweets: {

    1: {
        ...,
        text: "...",
        ...
    }
}
"""

def main():
    sent_file = open(sys.argv[1])
    tweet_file = open(sys.argv[2])

    scores = {}
    for line in sent_file:
        term, score = line.split("\t")
        scores[term] = int(score)

    # Lis
    tweet_data = []
    for line in tweet_file:
        response = json.loads(line)
        tweets = response["statuses"]
        for t in tweets:
            tweet_data.append(t["text"])


    # Pour chaque tweet
    i = 1
    for t in tweet_data:
        total = 0
        words = t.split()
        for w in words:
            # Supprime les lien web et autres mots indésirables
            if w.startswith("RT") or w.startswith("www") or w.startswith("http"):
                words.remove(w)

        # Filtre les mots des tweets
        pattern = re.compile('[^A-Za-z0-9]+')
        words = [pattern.sub("", w) for w in words]  # Sans lambda

        # Calcul la somme de polarité
        for w in words:
            if w in scores:
                total = total + scores[w]

        print(total)
        i += 1


if __name__ == '__main__':
    main()