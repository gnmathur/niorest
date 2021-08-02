- [ ] Parse HTTP response
- [ ] Fix error in periodic timers post retry connection
- [ ] Fix write errors
- [ ] Handle large HTTP responses  
- [ ] Add HTTP auth
- [ ] Reactor `write` should be able to get metadata from the ch information. Logging failures can then have the proper c
  contextual information
- [ ] Reactor should not need to understand `Task`. A `Task` is just another Reactee. Introduce Reactee