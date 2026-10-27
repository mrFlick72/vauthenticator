# tags variables
variable "common_resource_tags" {
  type = map(string)
}

# iam variables
variable "username" {
  type = string
}
variable "path" {
  type = string
}

# s3 variables
variable "vauthenticator_document_s3_bucket_name" {
  type = string
}
variable "vauthenticator_management_ui_document_s3_bucket_name" {
  type = string
}

# kms variables
variable "key_name" {
  type = string
}
variable "key_administrator_account_ids" {
  type = set(string)
}

variable "key_user_account_ids" {
  type = set(string)
}

variable "key_alias" {
  type = string
}
variable "key_description" {
  type = string
}
variable "deletion_window_in_days" {
  default = 7
}
