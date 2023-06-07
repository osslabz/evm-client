EvmClient
============
![GitHub](https://img.shields.io/github/license/osslabz/evm-client)
![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/osslabz/evm-client/maven.yml?branch=main)
[![Maven Central](https://img.shields.io/maven-central/v/net.osslabz/evm-client?label=Maven%20Central)](https://search.maven.org/artifact/net.osslabz/evm-client)

EvmClient is a thin wrapper around [web3j](https://github.com/web3j/web3j "Web3j: Web3 Java Ethereum Ðapp API")  which
provides some convenient methods to get ERC20 contract details, check balances etc.

Features:
---------

- All returned objects are pure DTO/POJOs without any internal state except the data. They can be easily serialized (
  e.g. to JSON) or passed to a cache or session layer.
- All Coins (primary currency of a given chain), token (ERC-20 compatible tokens) and balance objects carry the chain they belong to. This makes it easy to handle multiple chains in one application without risking loosing context.
- web3j (sometimes) throws unclassified checked `java.lang.Exception`. Those are wrapped in a
  RuntimeException (`net.osslabz.evmclient.EvmClientException`) so you don't have the handle/declare them. This is
  especially useful in an environment where a higher layer handles exceptions in a unified fashion (e.g. Spring).

QuickStart
---------

Maven
------

```xml

<dependency>
    <groupId>net.osslabz</groupId>
    <artifactId>evm-client</artifactId>
    <version>0.0.14</version>
</dependency>
```

Usage
------

Connect to Avalanche Mainnet and query information about wrapped AVAX:

```java
    EvmClient evmClient = new EvmClient(Chain.AVALANCHE_MAIN);
    Erc20Token tokenInfo = evmClient.getTokenInfo("0xb31f66aa3c1e785363f0875a1b74e27b85fd66c7"); // wrapped AVAX
```        

returns to following object:

```
Erc20Token(
  chain=Chain(
    name=Avalanche Network, 
    symbol=AVAX, 
    type=MAIN, 
    id=43114
  ),
  contractAddress=0xb31f66aa3c1e785363f0875a1b74e27b85fd66c7,
  name=Wrapped AVAX,
  symbol=WAVAX,
  decimals=18,
  totalSupply=17216093347349015614973039
)
```

Logging
------
This project uses slf4j-api but doesn't package an implementation. This is up to the using application. For the
tests logback is backing slf4j as implementation, with a default configuration logging to STOUT.


Compatibility
------
evm-client targets Java 1.8. It should run fine on Android but this hasn't been tested yet. If you use it on Android please let me know.