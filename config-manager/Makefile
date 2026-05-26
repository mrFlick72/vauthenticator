.PHONY: build run test tidy

build:
	go build -o bin/config-manager ./cmd/config-manager

run:
	go run ./cmd/config-manager

test:
	go test ./...

tidy:
	go mod tidy

