data "aws_iam_policy_document" "vauthenticator_sns_send_sms_policy" {
  statement {
    actions = [
      "sns:Publish"
    ]

    not_resources = ["arn:aws:sns:*:*:*"]
  }
}

resource "aws_iam_policy" "vauthenticator_sns_send_sms_iam_policy" {
  name = "sns_${var.username}_policy"
  path = var.path

  policy = data.aws_iam_policy_document.vauthenticator_sns_send_sms_policy.json
}

resource "aws_iam_user_policy_attachment" "vauthenticator_sns_send_sms_iam_policy-attach" {
  user       = data.aws_iam_user.vauthenticator.user_name
  policy_arn = aws_iam_policy.vauthenticator_sns_send_sms_iam_policy.arn
}