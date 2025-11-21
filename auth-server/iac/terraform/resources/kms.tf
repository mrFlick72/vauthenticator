module "kms" {
  source  = "terraform-aws-modules/kms/aws"
  version = "1.5.0"

  # Aliases
  aliases = ["alias/${var.key_alias}"]

  enable_key_rotation      = false
  description              = var.key_description
  customer_master_key_spec = "SYMMETRIC_DEFAULT"
  key_usage                = "ENCRYPT_DECRYPT"
  deletion_window_in_days  = var.deletion_window_in_days

  # Policy
  enable_default_policy = true
  key_administrators    = var.key_administrator_account_ids
  key_users             = var.key_user_account_ids


  tags = merge(tomap({ "Name" = var.key_name }), var.common_resource_tags)
}