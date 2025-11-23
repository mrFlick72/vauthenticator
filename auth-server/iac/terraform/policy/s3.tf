data "aws_s3_bucket" "vauthenticator_document_s3_bucket" {
  bucket = var.vauthenticator_document_s3_bucket_name
}

data "aws_iam_policy_document" "vauthenticator_document_bucket_policy" {
  statement {
    principals {
      type        = "AWS"
      identifiers = [data.aws_iam_user.vauthenticator.arn]
    }

    actions = [
      "s3:*",
    ]

    resources = [
      "arn:aws:s3:::${var.vauthenticator_document_s3_bucket_name}",
      "arn:aws:s3:::${var.vauthenticator_document_s3_bucket_name}/*"
    ]
  }
}

resource "aws_s3_bucket_policy" "vauthenticator_document_s3_bucket" {
  bucket = data.aws_s3_bucket.vauthenticator_document_s3_bucket.id
  policy = data.aws_iam_policy_document.vauthenticator_document_bucket_policy.json
}

////// user policy
data "aws_iam_policy_document" "vauthenticator_user_s3_policy" {
  statement {
    actions = [
      "s3:*"
    ]
    resources = [
      "arn:aws:s3:::${var.vauthenticator_document_s3_bucket_name}",
      "arn:aws:s3:::${var.vauthenticator_document_s3_bucket_name}/*",
      "arn:aws:s3:::${var.vauthenticator_management_ui_document_s3_bucket_name}",
      "arn:aws:s3:::${var.vauthenticator_management_ui_document_s3_bucket_name}/*"
    ]
  }
}

resource "aws_iam_policy" "vauthenticator_user_s3_iam_policy" {
  name = "s3_${var.username}_policy"
  path = var.path

  policy = data.aws_iam_policy_document.vauthenticator_user_s3_policy.json
}

resource "aws_iam_user_policy_attachment" "vauthenticator_user_s3_iam_policy-attach" {
  user       = data.aws_iam_user.vauthenticator.user_name
  policy_arn = aws_iam_policy.vauthenticator_user_s3_iam_policy.arn
}

//////////////////

data "aws_s3_bucket" "vauthenticator_management_ui_document_s3_bucket" {
  bucket = var.vauthenticator_management_ui_document_s3_bucket_name
}

data "aws_iam_policy_document" "vauthenticator_management_ui_document_bucket_policy" {
  statement {
    principals {
      type        = "AWS"
      identifiers = [data.aws_iam_user.vauthenticator.arn]
    }

    actions = [
      "s3:*",
    ]

    resources = [
      "arn:aws:s3:::${var.vauthenticator_management_ui_document_s3_bucket_name}",
      "arn:aws:s3:::${var.vauthenticator_management_ui_document_s3_bucket_name}/*"
    ]
  }
}

resource "aws_s3_bucket_policy" "vauthenticator_management_ui_document_s3_bucket" {
  bucket = data.aws_s3_bucket.vauthenticator_management_ui_document_s3_bucket.id
  policy = data.aws_iam_policy_document.vauthenticator_management_ui_document_bucket_policy.json
}