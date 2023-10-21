
## Making Changes

Depending on your changes there are certain rules you have to follow if you expect
your Pull Request to be merged.

**Note**: It is recommended to create a new remote branch for each Pull Request. 
Based on the current `master` changes!

1. Adding a new Method or Class
    - If your addition is not internal (e.g. an impl class or private method) you have to write documentation.
        - For that please follow the [JavaDoc template](https://jda.wiki/contributing/structure-guide/#javadoc)
    - Keep your code consistent! [example](https://jda.wiki/contributing/contributing/#making-changes)
        - Follow the guides provided at [JDA Structure Guide](https://jda.wiki/contributing/structure-guide/)
        - Compare your code style to the one used all over JDA and ensure you
          do not break the consistency (if you find issues in the JDA style you can include and update it)
    - Do not remove existing functionality, use deprecation instead (for reference [deprecation policy](https://github.com/discord-jda/JDA?tab=readme-ov-file#versioning-and-deprecation-policy))

2. Making a Commit
    - While having multiple commits can help the reader understand your changes, it might sometimes be
      better to include more changes in a single commit.
    - When you commit your changes write a proper commit caption which explains what you have done

3. Updating your Fork
    - Before you start committing make sure your fork is updated.
      (See [Syncing a Fork](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/working-with-forks/syncing-a-fork)
      or [Keeping a Fork Updated](https://thoughtbot.com/blog/keeping-a-github-fork-updated))
      
4. Only open Pull Requests to master
    - Look at the [Repository Structure](https://jda.wiki/contributing/repository-structure/) for further details
      
For more information please consult the [Contributing](https://jda.wiki/contributing/contributing/)
section of our wiki.
