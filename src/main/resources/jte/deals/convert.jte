@import ru.mentee.power.crm.model.Lead

@param Lead lead

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Конвертировать лид в сделку</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 p-8">
<div class="max-w-md mx-auto">
    <h1 class="text-2xl font-bold mb-6">Конвертировать лид в сделку</h1>
    <form method="post" action="/deals/convert" class="space-y-4">
        <input type="hidden" name="leadId" value="${lead.id().toString()}">

        <div>
            <label class="block text-sm font-medium mb-1">Клиент</label>
            <p class="p-2 bg-white rounded">${lead.name()}</p>
        </div>

        <div>
            <label class="block text-sm font-medium mb-1">Сумма сделки (₽)</label>
            <input type="number" step="0.01" name="amount" required
                   class="w-full px-3 py-2 border border-gray-300 rounded">
        </div>

        <button type="submit" class="w-full bg-green-500 text-white py-2 rounded">
            Создать сделку
        </button>
        <a href="/leads" class="block text-center text-gray-600 mt-2">Отмена</a>
    </form>
</div>
</body>
</html>