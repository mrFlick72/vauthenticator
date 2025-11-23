resource "aws_dynamodb_table" "client_application_table" {
  name         = "${var.client_application_table_name}${var.table_name_suffix}"
  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "client_id"


  attribute {
    name = "client_id"
    type = "S"
  }

  tags = merge(tomap({ "Name" = var.client_application_table_name }), var.common_resource_tags)
}

resource "aws_dynamodb_table" "account_table" {
  name = "${var.account_table_name}${var.table_name_suffix}"

  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "user_name"

  attribute {
    name = "user_name"
    type = "S"
  }

  tags = merge(tomap({ "Name" = var.account_table_name }), var.common_resource_tags)
}

resource "aws_dynamodb_table" "role_table" {
  name = "${var.role_table_name}${var.table_name_suffix}"

  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "role_name"

  attribute {
    name = "role_name"
    type = "S"
  }

  tags = merge(tomap({ "Name" = var.role_table_name }), var.common_resource_tags)
}

resource "aws_dynamodb_table" "ticket_table" {
  name = "${var.ticket_table_name}${var.table_name_suffix}"

  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "ticket"

  attribute {
    name = "ticket"
    type = "S"
  }

  ttl {
    attribute_name = "ttl"
  }

  tags = merge(tomap({ "Name" = var.ticket_table_name }), var.common_resource_tags)
}

resource "aws_dynamodb_table" "mfa_account_methods_table" {
  name = "${var.mfa_account_methods_table_name}${var.table_name_suffix}"

  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "user_name"
  range_key    = "mfa_channel"

  attribute {
    name = "user_name"
    type = "S"
  }

  attribute {
    name = "mfa_channel"
    type = "S"
  }

  attribute {
    name = "mfa_device_id"
    type = "S"
  }

  global_secondary_index {
    name            = "${var.mfa_account_methods_table_name}${var.table_name_suffix}_Index"
    hash_key        = "mfa_device_id"
    projection_type = "ALL"
  }

  tags = merge(tomap({ "Name" = var.mfa_account_methods_table_name }), var.common_resource_tags)
}

resource "aws_dynamodb_table" "default_mfa_account_methods_table" {
  name = "${var.default_mfa_account_methods_table_name}${var.table_name_suffix}"

  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "user_name"

  attribute {
    name = "user_name"
    type = "S"
  }

  tags = merge(tomap({ "Name" = var.default_mfa_account_methods_table_name }), var.common_resource_tags)
}

resource "aws_dynamodb_table" "mfa_keys_table" {
  name = "${var.mfa_keys_table_name}${var.table_name_suffix}"

  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "key_id"

  attribute {
    name = "key_id"
    type = "S"
  }

  ttl {
    attribute_name = "key_expiration_date_timestamp"
  }

  tags = merge(tomap({ "Name" = var.mfa_keys_table_name }), var.common_resource_tags)
}

resource "aws_dynamodb_table" "signature_keys_table" {
  name = "${var.signature_keys_table_name}${var.table_name_suffix}"

  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "key_id"

  attribute {
    name = "key_id"
    type = "S"
  }

  ttl {
    attribute_name = "key_expiration_date_timestamp"
  }

  tags = merge(tomap({ "Name" = var.signature_keys_table_name }), var.common_resource_tags)
}

resource "aws_dynamodb_table" "password_history_table" {
  name = "${var.password_history_table_name}${var.table_name_suffix}"

  billing_mode = "PAY_PER_REQUEST"
  hash_key     = "user_name"
  range_key    = "created_at"

  attribute {
    name = "user_name"
    type = "S"
  }
  attribute {
    name = "created_at"
    type = "N"
  }

  tags = merge(tomap({ "Name" = var.password_history_table_name }), var.common_resource_tags)
}