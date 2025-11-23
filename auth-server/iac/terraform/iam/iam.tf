resource "aws_iam_user" "vauthenticator" {
  name = var.username
  path = var.path

  tags = merge(tomap({ "Name" = var.username }), var.common_resource_tags)
}