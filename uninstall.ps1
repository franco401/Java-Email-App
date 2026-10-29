# stop the actively running Docker containers
docker stop spring-app react-app

# delete the Docker container
docker rm spring-app react-app

# delete the Docker image
docker rmi spring-image react-image

# spring-image should be gone
Write-OutPut `n` `n`
Write-OutPut Docker images:`n`
docker images
Write-OutPut `n` `n`

# react-app and spring-app should be gone
Write-OutPut Docker containers:`n`
sudo docker container ps -a
Write-OutPut `n` `n`

Write-Output If BOTH lists of Docker images and containers ARE EMPTY, then everything has been successfully uninstalled from your computer!
