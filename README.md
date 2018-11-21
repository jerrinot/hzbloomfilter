# Hazelcast Bloom Filters

How to use Hazelcast and Guava to create a distributed Bloom Filter.

It demontrates power of entry processors: You can use them to build new data-structure on top of Hazelcast. With 0 Hazelcast modifications. 

This is merely a PoC. There is still a massive room for improvements. For example:
- Support other types than plain integers
- Do not send filter configuration with every `put()`
- Do not send the whole object over the wire. Sufficiently wide hash is all what we need
- User a smaller bloom filter implementation to rid-off Guava dependency
- Tests

Contributions are very much appreciated. 
