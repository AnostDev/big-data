# Initiation MongoDB
**Chriss Santi**
**Amen Amegnonan**

## Q1: Prise en main

La base de donnée contient des objets json qui représentent chacun une ville. Chaque object a un identifiant `_id` le nom d'une ville `city`, une localisation `loc` qui est une liste contenant deux nombres, un cham `pop` (code postal) et l'état de la ville `state`.

Avec la commande `db.cities.count`, nous comptons données.

**Output**
```json
 db.cities.find().pretty()
{
	"_id" : "01002",
	"city" : "CUSHMAN",
	"loc" : [
		-72.51565,
		42.377017
	],
	"pop" : 36963,
	"state" : "MA"
}
{
	"_id" : "01012",
	"city" : "CHESTERFIELD",
	"loc" : [
		-72.833309,
		42.38167
	],
	"pop" : 177,
	"state" : "MA"
}

```



## Q2: Première région

Après avoir ajouté un nouveau shard `shard1`, la commade `sh.stats` retourne le status de la base de donnée. Notamment le champ
```json
  shards:
        {  "_id" : "shard1",  "host" : "localhost:27021" }
```
indique que le nouveau shard est bien ajouté. La commande aussi retourne l'identifiant du cluster `clusterId`. De plus la commande montre des bases de données présentent dans le shard. Nous remarquons donc la présence de `mydb`.
```json
  databases:
        {  "_id" : "mydb",  "primary" : "shard1",  "partitioned" : false }
``` 

**Output**
```json
--- Sharding Status --- 
  sharding version: {
  	"_id" : 1,
  	"minCompatibleVersion" : 5,
  	"currentVersion" : 6,
  	"clusterId" : ObjectId("5ddcf7b035ff7cf85e7d09ca")
  }
  shards:
        {  "_id" : "shard1",  "host" : "localhost:27021" }
  active mongoses:
        "3.2.22" : 1
  balancer:
        Currently enabled:  yes
        Currently running:  no
        Failed balancer rounds in last 5 attempts:  0
        Migration Results for the last 24 hours: 
                No recent migrations
  databases:
        {  "_id" : "mydb",  "primary" : "shard1",  "partitioned" : false }
```


## Q3: Première partition


Après avoir créer la nouvelle collection, la commande `sh.status` retourne le status de mis-à-jour du cluster. Une partition a été créée sur `shard1` avec un range sur `state:1` de `min=1` et `max=1`


```sh
use mydb
db.createCollection("cities1")
sh.enableSharding("mydb")
sh.shardCollection("mydb.cities1", { "state": 1} )
sh.status()
```

**Output**
```json

--- Sharding Status --- 
  sharding version: {
  	"_id" : 1,
  	"minCompatibleVersion" : 5,
  	"currentVersion" : 6,
  	"clusterId" : ObjectId("5ddcf7b035ff7cf85e7d09ca")
  }
  shards:
        {  "_id" : "shard1",  "host" : "localhost:27021" }
  active mongoses:
        "3.2.22" : 1
  balancer:
        Currently enabled:  yes
        Currently running:  no
        Failed balancer rounds in last 5 attempts:  0
        Migration Results for the last 24 hours: 
                No recent migrations
  databases:
        {  "_id" : "mydb",  "primary" : "shard1",  "partitioned" : true }
                mydb.cities1
                        shard key: { "state" : 1 }
                        unique: false
                        balancing: true
                        chunks:
                                shard1	1
                        { "state" : { "$minKey" : 1 } } -->> { "state" : { "$maxKey" : 1 } } on : shard1 Timestamp(1, 0)
```



## Q4: Deuxième région: Plus de partitons

L'ajout des données dans la nouvelle collection a engendré la création de 2 autres partitions.
La première partition
```json
{ "state" : { "$minKey" : 1 } } -->> { "state" : "MA" } on : shard1 Timestamp(1, 1) 
```
comporte sur les villes ayant pour état entre `minKey` et `RI`
La seconde partition 
```json
{ "state" : "MA" } -->> { "state" : "RI" } on : shard1 Timestamp(1, 2) 
```
comporte sur les ville ayant pour état entre  `MA` et `RI`.
La 3e partition
```json
{ "state" : "RI" } -->> { "state" : { "$maxKey" : 1 } } on : shard1 Timestamp(1, 3) 
```
comporte sur les villes ayant pour états entre `RI` et jusqu'à clé max.

```sh
db.createCollection("cities2")
db.cities.find().forEach(
 function(d) {
 db.cities1.insert(d);
 }
)
sh.status
```

**Output**
```json

--- Sharding Status --- 
  sharding version: {
  	"_id" : 1,
  	"minCompatibleVersion" : 5,
  	"currentVersion" : 6,
  	"clusterId" : ObjectId("5ddcf7b035ff7cf85e7d09ca")
  }
  shards:
        {  "_id" : "shard1",  "host" : "localhost:27021" }
  active mongoses:
        "3.2.22" : 1
  balancer:
        Currently enabled:  yes
        Currently running:  no
        Failed balancer rounds in last 5 attempts:  0
        Migration Results for the last 24 hours: 
                No recent migrations
  databases:
        {  "_id" : "mydb",  "primary" : "shard1",  "partitioned" : true }
                mydb.cities1
                        shard key: { "state" : 1 }
                        unique: false
                        balancing: true
                        chunks:
                                shard1	3
                        { "state" : { "$minKey" : 1 } } -->> { "state" : "MA" } on : shard1 Timestamp(1, 1) 
                        { "state" : "MA" } -->> { "state" : "RI" } on : shard1 Timestamp(1, 2) 
                        { "state" : "RI" } -->> { "state" : { "$maxKey" : 1 } } on : shard1 Timestamp(1, 3) 
```



## Q5: Encore plus de partitions

La création de la collection `cities2` a engendré la création de deux nouveaux partitionnements. En effet cela est du par le fait que la collection cities2 comporte une fonction de hashage qui sera utilisée pour partitionner les données dans cette collection. Nous rearquons notament que les intervales de partitionnements varient entre `minKey ---> NumberLong(0)` et `NumberLong(0) ---> maxKey`.


```sh
db.createCollection("cities2")
sh.shardCollection("mydb.cities2", { "state": "hashed"} )
```

**Output**
```json

--- Sharding Status --- 
  sharding version: {
  	"_id" : 1,
  	"minCompatibleVersion" : 5,
  	"currentVersion" : 6,
  	"clusterId" : ObjectId("5ddcf7b035ff7cf85e7d09ca")
  }
  shards:
        {  "_id" : "shard1",  "host" : "localhost:27021" }
  active mongoses:
        "3.2.22" : 1
  balancer:
        Currently enabled:  yes
        Currently running:  no
        Failed balancer rounds in last 5 attempts:  0
        Migration Results for the last 24 hours: 
                No recent migrations
  databases:
        {  "_id" : "mydb",  "primary" : "shard1",  "partitioned" : true }
                mydb.cities1
                        shard key: { "state" : 1 }
                        unique: false
                        balancing: true
                        chunks:
                                shard1	3
                        { "state" : { "$minKey" : 1 } } -->> { "state" : "MA" } on : shard1 Timestamp(1, 1) 
                        { "state" : "MA" } -->> { "state" : "RI" } on : shard1 Timestamp(1, 2) 
                        { "state" : "RI" } -->> { "state" : { "$maxKey" : 1 } } on : shard1 Timestamp(1, 3) 
                mydb.cities2
                        shard key: { "state" : "hashed" }
                        unique: false
                        balancing: true
                        chunks:
                                shard1	2
                        { "state" : { "$minKey" : 1 } } -->> { "state" : NumberLong(0) } on : shard1 Timestamp(1, 1) 
                        { "state" : NumberLong(0) } -->> { "state" : { "$maxKey" : 1 } } on : shard1 Timestamp(1, 2) 


```


```json
{ "state" : { "$minKey" : 1 } } -->> { "state" : NumberLong(0) } on : shard1 Timestamp(1, 1) 
                        { "state" : NumberLong(0) } -->> { "state" : { "$maxKey" : 1 } } on : shard1 
```

## Q6
Après population de la base de donnée, deux nouveaux partitionnements ont été créé. Les intervales de partitionnment pour la colleciton `cities2` varient maintenant entre:
- `NumberLong(0) } -->> NumberLong("3630192931154748514")`
- `NumberLong(3630192931154748514) } -->> NumberLong("8213220138195528769")`
- `NumberLong(8213220138195528769) } -->> NumberLong("8213220138195528769")`
- `NumberLong(8213220138195528769) } -->> maxKey`

**Output**
```json

mongos> sh.status()
--- Sharding Status --- 
  sharding version: {
  	"_id" : 1,
  	"minCompatibleVersion" : 5,
  	"currentVersion" : 6,
  	"clusterId" : ObjectId("5ddcf7b035ff7cf85e7d09ca")
  }
  shards:
        {  "_id" : "shard1",  "host" : "localhost:27021" }
...
                                shard1	4
                        { "state" : { "$minKey" : 1 } } -->> { "state" : NumberLong(0) } on : shard1 Timestamp(1, 1) 
                        { "state" : NumberLong(0) } -->> { "state" : NumberLong("3630192931154748514") } on : shard1 Timestamp(1, 3) 
                        { "state" : NumberLong("3630192931154748514") } -->> { "state" : NumberLong("8213220138195528769") } on : shard1 Timestamp(1, 4) 
                        { "state" : NumberLong("8213220138195528769") } -->> { "state" : { "$maxKey" : 1 } } on : shard1 Timestamp(1, 5) 
```

## 4.4.1
## Ajout de nouvelle région

**Output**
```json

--- Sharding Status --- 
  sharding version: {
  	"_id" : 1,
  	"minCompatibleVersion" : 5,
  	"currentVersion" : 6,
  	"clusterId" : ObjectId("5ddcf7b035ff7cf85e7d09ca")
  }
  shards:
        {  "_id" : "shard1",  "host" : "localhost:27021" }
        {  "_id" : "shard2",  "host" : "localhost:27022" }
...
                                shard1	4
                        { "state" : { "$minKey" : 1 } } -->> { "state" : NumberLong(0) } on : shard1 Timestamp(1, 1) 
                        { "state" : NumberLong(0) } -->> { "state" : NumberLong("3630192931154748514") } on : shard1 Timestamp(1, 3) 
                        { "state" : NumberLong("3630192931154748514") } -->> { "state" : NumberLong("8213220138195528769") } on : shard1 Timestamp(1, 4) 
                        { "state" : NumberLong("8213220138195528769") } -->> { "state" : { "$maxKey" : 1 } } on : shard1 Timestamp(1, 5) 

```



## Q7: Tags

```sh
sh.addShardTag("shard1", "CA")
sh.addShardTag("shard2", "NY")
sh.addShardTag("shard3", "Others")
```

Ces commandes ont permies de renomme le partitionnement des données dans les régions. Ainsi les données des villes de l'état de  `CA` sont dans la première région, celles de l'état `NY` dans la deuxième région et toutes les autres `other` dans la troixième région.

**Output**
```json

--- Sharding Status --- 
  sharding version: {
  	"_id" : 1,
  	"minCompatibleVersion" : 5,
  	"currentVersion" : 6,
  	"clusterId" : ObjectId("5ddcf7b035ff7cf85e7d09ca")
  }
  shards:
        {  "_id" : "shard1",  "host" : "localhost:27021",  "tags" : [ "CA" ] }
        {  "_id" : "shard2",  "host" : "localhost:27022",  "tags" : [ "NY" ] }
        {  "_id" : "shard3",  "host" : "localhost:27023",  "tags" : [ "Others" ] }
...
                                shard3	4
                        { "state" : { "$minKey" : 1 } } -->> { "state" : "CA" } on : shard3 Timestamp(4, 1) 
                        { "state" : "CA" } -->> { "state" : "CA_" } on : shard3 Timestamp(4, 3) 
                        { "state" : "CA_" } -->> { "state" : "MA" } on : shard3 Timestamp(4, 4) 
                        { "state" : "MA" } -->> { "state" : "RI" } on : shard3 Timestamp(3, 0) 
                        { "state" : "RI" } -->> { "state" : { "$maxKey" : 1 } } on : shard1 Timestamp(3, 1) 
                         tag: Others  { "state" : { "$minKey" : 1 } } -->> { "state" : "CA" }
                         tag: CA  { "state" : "CA" } -->> { "state" : "CA_" }
                         tag: Others  { "state" : "CA_" } -->> { "state" : "NY" }
                         tag: NY  { "state" : "NY" } -->> { "state" : "NY_" }
                         tag: Others  { "state" : "NY_" } -->> { "state" : { "$maxKey" : 1 } }

```

**NB:** Les Résultats des commandes ont été tronqués.