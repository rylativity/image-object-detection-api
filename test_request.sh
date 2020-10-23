#!/bin/bash

curl -XPOST localhost:8080/api/detect -F "image=@testimage1.jpg" -H "Content-Type: multipart/form-data"
