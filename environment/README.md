# Environment Setup

## Installation pre-requisites
The environment has been virtualized using [Docker](https://www.docker.com/).
In Linux environments, where Docker runs natively, it may make more sense to install and use
[Docker](https://docs.docker.com/installation/) directly.

In OSX or Windows environments, you can use [Vagrant](http://docs.vagrantup.com/v2/installation/) as an alternative to
the Docker Machine (formerly Boot2Docker) provided by Docker for non-linux environments.

Once you have decided whether to use Vagrant or standalone Docker:
1. Put a copy of your AEM quikstart jar in the directory: docker/aem/AEM_6.1_Quickstart.jar
2. Put a copy of your license file in the directory: docker/aem/license.properties
3. Follow the steps related to Vagrant or standalone Docker set up

## Vagrant set up
### Start up VM
```sh
$ vagrant up
```
### Dispatcher, Publish and Author urls
* Dispatcher: http://aem-reference
* Publish: http://aem-reference:4503
* Author: http://aem-reference:4502

### Other useful vagrant commans
**Vagrant help**
```sh
$ vagrant help
```
**SSH into the VM**
```sh
$ vagrant ssh
```

## Stand alone docker set up
1. Install Docker Compose as described in the [installation istruction](https://docs.docker.com/compose/install/)
2. Run docker compose
```sh
$ docker-compose up -d
```

### Dispatcher, Publish and Author urls
* Dispatcher: http://localhost
* Publish: http://localhost:4503
* Author: http://localhost:4502

