import fs from 'fs';

const file = 'app/src/main/java/com/example/mobile/presentation/viewmodel/HabitDetailViewModel.kt';
let content = fs.readFileSync(file, 'utf8');

// Fix the literal \n in import (from PowerShell script)
content = content.replace('import kotlinx.coroutines.Dispatchers`nimport kotlinx.coroutines.Job', 'import kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.Job');

// Verify both fixes are in place
const hasDispatchersImport = content.includes('import kotlinx.coroutines.Dispatchers\n');
const hasDispatchersDefault = content.includes('viewModelScope.launch(Dispatchers.Default)');

fs.writeFileSync(file, content, 'utf8');
console.log('hasDispatchersImport:', hasDispatchersImport);
console.log('hasDispatchersDefault:', hasDispatchersDefault);
console.log('Done');
