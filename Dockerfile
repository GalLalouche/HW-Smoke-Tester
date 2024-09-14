FROM ubuntu:24.04
RUN apt-get update && apt-get install -y build-essential curl libffi-dev libffi8ubuntu1 libgmp-dev libgmp10 libncurses-dev python3 python3-pip unzip zip git
# Copied from https://stackoverflow.com/a/72953383/736508
RUN curl --proto '=https' --tlsv1.2 -sSf https://get-ghcup.haskell.org | BOOTSTRAP_HASKELL_NONINTERACTIVE=1 BOOTSTRAP_HASKELL_GHC_VERSION=latest BOOTSTRAP_HASKELL_CABAL_VERSION=latest BOOTSTRAP_HASKELL_INSTALL_STACK=1 BOOTSTRAP_HASKELL_INSTALL_HLS=1 BOOTSTRAP_HASKELL_ADJUST_BASHRC=P sh
RUN curl -s "https://get.sdkman.io" | bash
SHELL ["/bin/bash", "-c"]
RUN source "/root/.sdkman/bin/sdkman-init.sh" \
                && sdk install java 17.0.2-tem \
                && sdk install sbt 1.10.1 \
                && sdk install scala 3.5.0
ENV PATH=/root/.sdkman/candidates/java/current/bin:$PATH
ENV PATH=/root/.sdkman/candidates/scala/current/bin:$PATH
ENV PATH=/root/.sdkman/candidates/sbt/current/bin:$PATH
RUN rm -rf https://github.com/GalLalouche/HW-Smoke-Tester.git
RUN git clone https://github.com/GalLalouche/HW-Smoke-Tester.git
WORKDIR HW-Smoke-Tester
RUN sbt compile
EXPOSE 8090
EXPOSE 5005

# RUN chmod +x deployment-service
CMD ["sbt", "~Jetty/start"]
