# start docker
Start-Process "C:\Program Files\Docker\Docker\Docker Desktop.exe"

# build the Docker images
cd emailapp
docker build -t spring-image .
cd ../frontend
docker build -t react-image .
Write-OutPut `n` `n`

# ensure the Docker images were built before running containers
Write-OutPut Docker images:`n`
docker images

# run the Docker containers based on the Docker images for the project
cd ../
docker run -d -p 127.0.0.1:8080:8080 --name spring-app spring-image
docker run -d -p 127.0.0.1:5173:5173 --name react-app react-image
Write-OutPut `n` `n`

# ensure the Docker containers are running
Write-OutPut Docker containers:`n`
docker container ps -a
Write-OutPut `n` `n`

Write-Output If "spring-image" is in Docker images AND and "spring-app" is in Docker containers, everything has been successfully setup and running!

Write-OutPut Applicaton running in: 127.0.0.1:5173

# open the web application
Start-Process "msedge.exe" 127.0.0.1:5173