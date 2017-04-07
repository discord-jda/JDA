
## Making Changes

Depending on your changes there are certain rules you have to follow if you expect
your Pull Request to be merged.

**Note**: It is recommended to create a new remote branch for each Pull Request. 
Based on the current `upstream/master` changes!

1. Adding a new Method or Class
    - If your addition is not internal (e.g. an impl class or private method) you have to write documentation.
        - For that please follow the [JavaDoc template](https://github.com/DV8FromTheWorld/JDA/wiki/6%29-JDA-Structure-Guide#javadoc)
    - Keep your code consistent! [example](https://github.com/DV8FromTheWorld/JDA/wiki/5%29-contributing#examples)
        - Follow the guides provided at [JDA Structure Guide](https://github.com/DV8FromTheWorld/JDA/wiki/6%29-JDA-Structure-Guide)
        - Compare your code style to the one used all over JDA and ensure you
          do not break the consistency (if you find issues in the JDA style you can include and update it)

2. Making a Commit
    - While having multiple commits can help the reader understand your changes, it might sometimes be
      better to include more changes in a single commit.
    - When you commit your changes write a proper commit caption which explains what you have done

3. Updating your Fork
    - Before you start committing make sure your fork is updated.
      (See [Syncing a Fork](https://help.github.com/articles/syncing-a-fork/)
      or [Keeping a Fork Updated](https://robots.thoughtbot.com/keeping-a-github-fork-updated))
      
For more information please consult the [Contributing](https://github.com/DV8FromTheWorld/JDA/wiki/5%29-Contributing)
section of our wiki.
