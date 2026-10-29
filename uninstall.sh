# stop the actively running Docker containers
sudo docker stop spring-app react-app

# delete the Docker containers
sudo docker rm spring-app react-app

# delete the Docker images
sudo docker rmi spring-image react-image

# react-image and spring-image should be gone
printf "\n\n"
printf "Docker images:\n"
sudo docker images
printf "\n\n"

# react-app and spring-app should be gone
printf "Docker containers:\n"
sudo docker container ps -a
printf "\n\n"

echo If BOTH lists of Docker images and containers ARE EMPTY, then everything has been successfully uninstalled from your computer!
