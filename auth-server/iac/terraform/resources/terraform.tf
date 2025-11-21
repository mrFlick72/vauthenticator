terraform {
  backend "s3" {
    key = "terraform-state/resources/state.tfstate"
  }

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.83.0"
    }
  }
}

provider "aws" {}