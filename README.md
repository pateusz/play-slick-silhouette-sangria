<!-- <meta name="google-site-verification" content="qwXl9E1GhGF71zyQaFwsQD1AD9ruqvNJfmVlTGab9kE" /> -->
# `Play Silhouette Sangria Slick REST Seed` 

Example project for Play Framework that wires up couple of components:

* [Scala 2.13](https://www.scala-lang.org/)
* [Play framework 2.8](https://www.playframework.com/)
* [Sangria 2.0](https://sangria-graphql.org/)
* [Slick 3](http://scala-slick.org/)
* [Silhouette 7](https://www.silhouette.rocks/)
* [H2 in memory DB](http://www.h2database.com)
* [GraphiQL graphical interface](https://github.com/graphql/graphiql)
* [Flyway migrations](https://flywaydb.org/)

Its aim is to showcase how these compenents can be wired together to provide secure graphql endpoints with authorization.
Authentication is built upon [JWT tokens](http://jwt.io).

## Basic usage
### Running app
```bash
sbt run
```
App runs on http 9000 by default.
Make sure you invoke db migrations.
After opening [`localhost:9000`](http://localhost:9000) you should see:
![DB migrate](https://i.imgur.com/BptHlxS.png)
Make sure you apply migration.

### Graphiql UI
Static graphiql UI is available at [`localhost:9000/graphiql`](http://localhost:9000/graphiql)
![GraphiQL UI](https://i.imgur.com/wGND90Q.png)


### Users
There are two users shipped with application:
| username    | password        | role       |
| ----------- |:----------------| ---------- |
| testuser    | test            |user        |
| testadmin   | test            |admin       |

Admin is able to add records to database via graphql query, user can only read from it.



### Sign-in

```bash
curl -X POST http://localhost:9000/auth/signin 
     -H 'Content-Type: application/json' 
     -d '{"identifier": "testuser", "password": "test"}' 
```

```
< HTTP/1.1 200 OK
{
    "token":"eyJ0eXAiOiJKV......this is long....XhGJk6f7yxgjA"
}   
```

### Sign-up
Despite having users shipped you may want to create new one:
```bash
curl -X POST http://localhost:9000/auth/signup \
     -H 'Content-Type: application/json' \
     -d '{
            "identifier": "newadminuser", 
            "password": "newpassword",
            "email": "newadmin@test.com", 
            "firstName": "newname", 
            "lastName": "newsurname", 
            "isAdmin": true
        }'
```

```
< HTTP/1.1 200 OK
{
  "token": "eyJ0eXAiOiJKV1QiLCJhbGci..."
}
```


### GraphQL queries

After acquiring JWT token (either by signin or signup methods) you may execute graphQL queries:

#### Query
This also showcases Sangria's deferred fetchers capability which allows to reference records between tables (sort of DB join but not quite).

```bash
curl  -X POST http://localhost:9000/graphql\                                                                                                                              
      -H 'Content-Type: application/json' \
      -H "X-Auth-Token: eyJ0eXAiOiJKV1Q....." \
      -d '{"query" : "{product(id: 1){ id name opinions {text}}}"}'
```

```javascript
< HTTP/1.1 200 OK
{
   "data":{
      "product":{
         "id":1,
         "name":"First product",
         "opinions":[
            {
               "text":"good"
            },
            {
               "text":"very good"
            }
         ]
      }
   }
}
```

#### Mutation
This requires admin account 
```bash
curl  -X POST http://localhost:9000/graphql\
    -H 'Content-Type: application/json' \
    -H "X-Auth-Token: eyJ0eXAiOi..." \
    -d '{"query" : "mutation { insertProduct(name: \"new product name\"){ id name}}"}'
```

```javascript
< HTTP/1.1 200 OK
{
  "data": {
    "insertProduct": {
      "id": 5,
      "name": "new product name"
    }
  }
}
```
In case executed with plain user privileges will get followig error:
```javascript
< HTTP/1.1 200 OK
{
  "data": null,
  "errors": [{
    "message": "You do not have permission to do this operation",
    "path": ["insertProduct"],
    "locations": [{
      "line": 1,
      "column": 12
    }]
  }]
}
```
or in case of DB intergrity constarint validation:
```javascript
{
  "data": null,
  "errors": [{
    "message": "product with given name already exists",
    "path": ["insertProduct"],
    "locations": [{
      "line": 1,
      "column": 12
    }]
  }]
}
```

