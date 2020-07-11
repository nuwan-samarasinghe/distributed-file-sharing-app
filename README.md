#Distributed File Sharing Application

### Group Members
>* Nuwan Samarasinghe (199360G)
>* Banu Liyanapathirana  (199380R)
>* Hasitha Maduranga     (199344L)
>* Chathura Siriwardhana (199366F)
#

### Application run configurations (we are using spring boot configuration)

- First execute : ```distributed-file-sharing-app\src\main\java\com\assignment\distributedfilesharingapp\server\BootstrapServer.java```
- For every run change this apache port : 
```server.port: 8081```
- To enable console mode : ```app.enable-console:true``` otherwise ```false```
- Then execute : ```DistributedFileSharingAppApplication.java```
#

### How to Test

#### Swagger method

Go to following link in your browser: ```https://localhost:8081/swagger-ui.html```
(change the port according to the ```server.port: 8081```)

![Image of Swagger](src/main/resources/doc-images/swagger.PNG)

__Following are the rest end points that can use__

* ```/file``` : get all the file list in the current node

![GET ALL FILES](src/main/resources/doc-images/get_all_files.PNG)

* ```/file{fileName}``` get file information per node

![GET FILES DETAIL](src/main/resources/doc-images/get_file_details.PNG)

* ```/file/download``` download file to application running server

![DOWNLOAD FILES](src/main/resources/doc-images/download_file.PNG)

* ```/ip-table``` get ip table of the current node

![GET IP TABLE](src/main/resources/doc-images/get_ip_table.PNG)

#### Console method

console ui

![CONSOLE UI](src/main/resources/doc-images/console_ui.PNG)

To print the ip table => use option (2) `<(Number 2 key then Enter)>`

To exit from the network => use option (3) `<(Number 3 key then Enter)>`

search files choose option 1 => use option (1) `<(Number 1 key then Enter)>`

![CONSOLE UI](src/main/resources/doc-images/console_search_download.PNG)

In the prompted ```Enter your search query below : ``` enter the file name

Then from the response list select the option number `<(Number key then Enter)>`

it will download the file to the server


### Questions and Answers

#### Question 01
How would you measure the performance of the system? 
##### Answer:
 * Measure the time to search a random file while increasing number of nodes in the system.
 * Generate dummy concurrent requests using JMeeter to download/search particular file and 
  measure the CPU and memory usage of a single node.In this way we could test the load and stress of the system.
#### Question 02
You have mentioned that the search request will be a recursive call. If so, will it be a blocking call? 
Will a particular node block, waiting for the response to arrive from the downstream nodes, unable to respond to other requests coming its way?
What is the technology stack you plan to use?
##### Answer:
* We have decoupled the message sender and receiver using blocking queues.
One queue for outgoing messages and other queue for incoming requests.
So the outgoing and incoming request will process independently from each other.

* Further it uses timeout time for each and every request.
So it won't badly hanging on to a single message process. 
#### Question 03
What is the technology stack you plan to use?
##### Answer:
* Socket Programming
* Multi-threading Techniques
* RESTful webservices
* Queue Technologies
* Network Communication Mechanisms
* Angular Framework
* Sprig Framework

