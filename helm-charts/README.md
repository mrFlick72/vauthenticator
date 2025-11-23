# VAuthenticator Helm Chart

It is the official helm chart for VAuthenticator ecosystem.

# Usage

Helm must be installed to use the charts. Please refer to Helm's documentation to get started.
Once Helm is set up properly, add the repo as follows:

```helm repo add vauthenticator https://vauthenticator.github.io/helm-charts```

You can then run helm search repo vauthenticator to see the charts.

> In order to use the repo run the dependency update command in order to update all the needed dependencies with this
command
> 
> ```helm dependency update vauthenticator```

if you want to use redis in kubernetes you can configure the helm charts to use a redis installation in the same
namespace
The embedded installation is the same coming from bitnami and for further redis configuration you can see on
the [bitnami github repository](https://github.com/bitnami/charts/tree/main/bitnami/redis/) 

Helm Chart documentation details can be reached on [this link](charts/README.md)