module "vauthenticator_s3_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.6.0"

  bucket                  = var.vauthenticator_document_s3_bucket_name
  force_destroy = true

  tags = merge(tomap({ "Name" = var.vauthenticator_document_s3_bucket_name }), var.common_resource_tags)
}
module "vauthenticator_management_ui_s3_bucket" {
  source  = "terraform-aws-modules/s3-bucket/aws"
  version = "4.6.0"

  bucket                  = var.vauthenticator_management_ui_document_s3_bucket_name
  force_destroy = true

  tags = merge(tomap({ "Name" = var.vauthenticator_management_ui_document_s3_bucket_name }), var.common_resource_tags)
}
