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

# dynamodb variables
variable "table_name_suffix" {
  type    = string
  default = ""
}


# dynamodb variables
variable "client_application_table_name" {
  type    = string
  default = "VAuthenticator_ClientApplication"
}
variable "account_table_name" {
  type    = string
  default = "VAuthenticator_Account"
}
variable "role_table_name" {
  type    = string
  default = "VAuthenticator_Role"
}
variable "ticket_table_name" {
  type    = string
  default = "VAuthenticator_Ticket"
}
variable "mfa_keys_table_name" {
  type    = string
  default = "VAuthenticator_Mfa_Keys"
}
variable "signature_keys_table_name" {
  type    = string
  default = "VAuthenticator_Signature_Keys"
}
variable "mfa_account_methods_table_name" {
  type    = string
  default = "VAuthenticator_Mfa_Account_Methods"
}
variable "default_mfa_account_methods_table_name" {
  type    = string
  default = "VAuthenticator_Default_Mfa_Account_Methods"
}
variable "password_history_table_name" {
  type    = string
  default = "VAuthenticator_Password_History"
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
