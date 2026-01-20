@import ru.mentee.power.crm.domain.Deal
@import ru.mentee.power.crm.domain.DealStatus
@param java.util.Map<DealStatus, java.util.List<Deal>> dealsByStatus

<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Воронка продаж</title>
    <script src="https://cdn.tailwindcss.com"></script>
</head>
<body class="bg-gray-100 p-8">
<h1 class="text-3xl font-bold mb-6">Воронка продаж (Kanban)</h1>

<div class="grid grid-cols-6 gap-4">
    @for(DealStatus status : DealStatus.values())
        <div class="bg-white rounded shadow-md p-4">
            <h2 class="font-bold mb-4 text-center">${status}</h2>

            @for(Deal deal : dealsByStatus.getOrDefault(status, java.util.List.of()))
                <div class="bg-gray-50 p-3 mb-2 rounded border">
                    <p class="font-semibold">${deal.getAmount()} ₽</p>
                    <p class="text-sm text-gray-600">Lead: ${deal.getLeadId().toString()}</p>

                    <!-- Форма перехода статуса -->
                    <form method="post" action="/deals/${deal.getId().toString()}/transition" class="mt-2">
                        <select name="newStatus" class="text-xs w-full mb-1 bg-white border rounded px-1 py-0.5">
                            <option value="" disabled selected>Выберите новый статус</option>
                            @for(DealStatus target : DealStatus.values())
                                @if(deal.getStatus() != target && deal.getStatus().canTransitionTo(target))
                                    <option value="${target}">→ ${target}</option>
                                @endif
                            @endfor
                        </select>
                        <button type="submit" class="text-xs bg-blue-500 text-white px-2 py-1 rounded hover:bg-blue-600">
                            Перевести
                        </button>
                    </form>
                </div>
            @endfor
        </div>
    @endfor
</div>
</body>
</html>