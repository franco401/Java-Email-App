# start docker
sudo systemctl start docker

# build the Docker images
cd emailapp
sudo docker build -t spring-image .
cd ../frontend
sudo docker build -t react-image .
printf "\n\n"

# ensure the Docker images were built before running containers
printf "Docker images:\n"
sudo docker images

# run the Docker containers based on the Docker images for the project
cd ../
sudo docker run -d -p 127.0.0.1:8080:8080 --name spring-app spring-image
sudo docker run -d -p 127.0.0.1:5173:5173 --name react-app react-image
printf "\n\n"

# ensure the Docker containers are running
printf "Docker containers:\n"
sudo docker container ps -a
printf "\n\n"

echo If "react-image" and "spring-image" are in Docker images AND "react-app" and "spting-app" are in Docker containers, everything has been successfully setup and running!

echo Applicaton running in: 127.0.0.1:5173

# open the web application
firefox 127.0.0.1:5173