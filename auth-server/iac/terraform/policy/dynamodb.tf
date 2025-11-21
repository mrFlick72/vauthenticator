
resource "aws_iam_user_policy_attachment" "dynamo_policy-attach" {
  user       = data.aws_iam_user.vauthenticator.user_name
  policy_arn = aws_iam_policy.dynamo_policy.arn
}

resource "aws_iam_policy" "dynamo_policy" {
  name = "dynamodb_${var.username}_policy"
  path = var.path

  policy = data.aws_iam_policy_document.dynamo_policy.json
}

data "aws_iam_policy_document" "dynamo_policy" {
  statement {

    actions = [
      "dynamodb:GetItem",
      "dynamodb:Scan",
      "dynamodb:PutItem",
      "dynamodb:DeleteItem",
      "dynamodb:Query"
    ]

    resources = [
      data.aws_dynamodb_table.client_application_table.arn,
      data.aws_dynamodb_table.account_table.arn,
      data.aws_dynamodb_table.role_table.arn,
      data.aws_dynamodb_table.ticket_table.arn,
      data.aws_dynamodb_table.mfa_account_methods_table.arn,
      data.aws_dynamodb_table.mfa_keys_table.arn,
      data.aws_dynamodb_table.signature_keys_table.arn,
      data.aws_dynamodb_table.password_history_table.arn,
      data.aws_dynamodb_table.default_mfa_account_methods_table.arn,
    ]
  }
}

data aws_dynamodb_table client_application_table {
  name = "${var.client_application_table_name}${var.table_name_suffix}"
}
data aws_dynamodb_table account_table {
  name = "${var.account_table_name}${var.table_name_suffix}"
}
data aws_dynamodb_table role_table {
  name = "${var.role_table_name}${var.table_name_suffix}"
}
data aws_dynamodb_table ticket_table {
  name = "${var.ticket_table_name}${var.table_name_suffix}"
}
data aws_dynamodb_table mfa_account_methods_table {
  name = "${var.mfa_account_methods_table_name}${var.table_name_suffix}"
}
data aws_dynamodb_table default_mfa_account_methods_table {
  name = "${var.default_mfa_account_methods_table_name}${var.table_name_suffix}"
}
data aws_dynamodb_table mfa_keys_table {
  name = "${var.mfa_keys_table_name}${var.table_name_suffix}"
}
data aws_dynamodb_table signature_keys_table {
  name = "${var.signature_keys_table_name}${var.table_name_suffix}"
}
data aws_dynamodb_table password_history_table {
  name = "${var.password_history_table_name}${var.table_name_suffix}"
}