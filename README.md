# URL Shortener and Redirector

A serverless application that shorten and redirects URLs.

## Overview

This project provides a serverless URL shortener built using AWS Lambda and Amazon S3. When a long URL is submitted, the
system generates a unique, random short URL and stores the mapping between the two in an S3 object. Upon accessing the
short URL, the Lambda function retrieves the original URL from S3 and performs a 301 redirect.

## Features

* Serverless architecture using AWS Lambda
* Supports HTTP and HTTPS requests
* Redirects shortened URLs to their original destinations
* Handles errors and edge cases

## Technologies

* AWS API Gateway
* AWS Lambda
* AWS S3
* Java 21
* Maven
* Lombok