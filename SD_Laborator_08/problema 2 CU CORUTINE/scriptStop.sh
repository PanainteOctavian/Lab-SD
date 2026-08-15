docker stop student_microservice_3 
docker stop student_microservice_2 
docker stop student_microservice_1 
docker stop teacher_microservice
docker stop message_manager
sudo sh -c 'truncate -s 0 /var/lib/docker/containers/*/*-json.log'
