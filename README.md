Tips for Setup the Projects
===========================

Git
---

### Clone the Repo to Local
    git clone git@github.com:telgniw/BukuDroid.git

### Commit Local Changes
    git add files_to_be_commited
    git commit -m "Comment for this commit."

### Push Local Commits to Repo
    git push origin master

Eclipse
-------

### Import Eclipse Project
`File`->`Import`->`Existing Projects into Workspace`->Select a Folder

### Setup Project Dependency
1. Import all projects `/Mobile/*`.
2. `ActionBar`->`Preferences`->`Android`: Ensure `Is Library` is checked.
3. `ViewPagerIndicator`->`Preferences`->`Android`: Ensure `Is Library` is checked.
4. `BukuDroid`->`Preferences`->`Android`: Add `ActionBar` and `ViewPagerIndicator` to `Library`.
