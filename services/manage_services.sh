#!/usr/bin/env bash

# Define the compose files
RABBITMQ_COMPOSE=$(realpath "./rabbitmq/docker-compose.yml")
POSTGRES_COMPOSE=$(realpath "./postgres/docker-compose.yml")

# Function to start services
start_services() {
    echo "Starting services..."
    podman-compose -f $RABBITMQ_COMPOSE up -d
    podman-compose -f $POSTGRES_COMPOSE up -d
    echo "Services started."
}

# Function to stop services
stop_services() {
    echo "Stopping services..."
    podman-compose -f $RABBITMQ_COMPOSE down
    podman-compose -f $POSTGRES_COMPOSE down
    echo "Services stopped."
}

# Check the argument and perform the appropriate action
case "$1" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    *)
        echo "Usage: $0 {start|stop}"
        exit 1
        ;;
esac

exit 0