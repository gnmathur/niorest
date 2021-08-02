- [ ] Parse HTTP response
- [x] Fix error in `TimerDb::smallestTimer` to return `intervalInMs`
- [x] Fix error in periodic timers post retry connection
- [x] Handle write errors
- [ ] Handle large HTTP responses
- [ ] Handle chunked HTTP responses  
- [ ] Add HTTP auth
- [ ] Reactor `write` should be able to get metadata from the ch information. Logging failures can then have the proper c
  contextual information
- [ ] Reactor should not need to understand `Task`. A `Task` is just another Reactee. Introduce Reactee