@import ru.mentee.power.crm.entity.Deal
@param java.util.List<Deal> deals

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список сделок</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 p-8">
<div class="max-w-6xl mx-auto">
    <div class="flex justify-between items-center mb-6">
        <h1 class="text-3xl font-bold">Сделки CRM</h1>
        <a href="/jpa-deals/kanban" class="bg-blue-500 text-white px-4 py-2 rounded">Воронка продаж</a>
    </div>

    <table class="w-full bg-white shadow-md rounded">
        <thead class="bg-gray-200">
        <tr>
            <th class="p-3 text-left">ID</th>
            <th class="p-3 text-left">Сумма</th>
            <th class="p-3 text-left">Статус</th>
            <th class="p-3 text-left">Lead ID</th>
            <th class="p-3 text-left">Дата создания</th>
        </tr>
        </thead>
        <tbody>
        @for(Deal deal : deals)
            <tr class="border-b">
                <td class="p-3">${deal.getId().toString()}</td>
                <td class="p-3">${deal.getAmount()} ₽</td>
                <td class="p-3">
                    <span class="px-2 py-1 rounded text-sm ${deal.getStatus().name().equals("WON") ? "bg-green-100" : deal.getStatus().name().equals("LOST") ? "bg-red-100" : "bg-blue-100"}">${deal.getStatus()}</span>
                </td>
                <td class="p-3">${deal.getLeadId().toString()}</td>
                <td class="p-3">${deal.getCreatedAt().toString()}</td>
            </tr>
        @endfor
        </tbody>
    </table>
</div>
</body>
</html>