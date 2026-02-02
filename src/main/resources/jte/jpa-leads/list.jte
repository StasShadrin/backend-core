@import ru.mentee.power.crm.entity.Lead
@import ru.mentee.power.crm.model.LeadStatus
@import java.util.List

@param String search
@param List<Lead> leads
@param LeadStatus currentFilter

@template.layout.main(content = @`
    <form method="get" action="/jpa-leads" class="mb-6 bg-white p-4 rounded shadow">
        <div class="flex gap-4">
            <input
                    type="text"
                    name="search"
                    placeholder="Поиск по имени, email или компании..."
                    value="${search}"
                    class="px-3 py-2 border rounded w-full"
            />
            <select name="status" class="px-3 py-2 border rounded">
                <option value="">Все статусы</option>
                <option value="NEW">Новый</option>
                <option value="CONTACTED">Связались</option>
                <option value="QUALIFIED">Квалифицирован</option>
                <option value="LOST">Потерян</option>
            </select>
            <button type="submit" class="bg-blue-500 text-white px-4 py-2 rounded">Поиск</button>
        </div>
        <a href="/jpa-leads" class="text-sm text-gray-600 hover:underline">Очистить фильтры</a>
    </form>

    <div class="mb-4 flex justify-between items-center">
        <div class="mb-4 flex gap-2">
            <a href="/jpa-leads"
               class="px-4 py-2 rounded ${currentFilter == null ? "bg-blue-500 text-white" : "bg-gray-200"}">
                Все
            </a>
            <a href="/jpa-leads?status=NEW"
               class="px-4 py-2 rounded ${currentFilter == LeadStatus.NEW ? "bg-blue-500 text-white" : "bg-gray-200"}">
                NEW
            </a>
            <a href="/jpa-leads?status=CONTACTED"
               class="px-4 py-2 rounded ${currentFilter == LeadStatus.CONTACTED ? "bg-blue-500 text-white" : "bg-gray-200"}">
                CONTACTED
            </a>
            <a href="/jpa-leads?status=QUALIFIED"
               class="px-4 py-2 rounded ${currentFilter == LeadStatus.QUALIFIED ? "bg-blue-500 text-white" : "bg-gray-200"}">
                QUALIFIED
            </a>
            <a href="/jpa-leads?status=LOST"
               class="px-4 py-2 rounded ${currentFilter == LeadStatus.LOST ? "bg-blue-500 text-white" : "bg-gray-200"}">
                LOST
            </a>
        </div>
        <a href="/jpa-leads/new"
           class="bg-green-500 text-white px-4 py-2 rounded hover:bg-green-600">
            + Добавить лида
        </a>
    </div>

    @if(currentFilter != null)
        <p class="text-sm text-gray-600 mb-2">Показаны лиды со статусом: ${currentFilter}</p>
    @endif

    <div class="bg-white rounded-lg shadow-md p-6">
        <h2 class="text-2xl font-bold mb-4">Lead List</h2>

        <table class="min-w-full bg-white border border-gray-200">
            <thead class="bg-gray-100">
            <tr>
                <th class="px-4 py-2 text-left">Name</th>
                <th class="px-4 py-2 text-left">Email</th>
                <th class="px-4 py-2 text-left">Phone</th>
                <th class="px-4 py-2 text-left">Company</th>
                <th class="px-4 py-2 text-left">Status</th>
                <th class="px-4 py-2 text-left">Edit</th>
                <th class="px-4 py-2 text-left">Delete</th>
                <th class="px-4 py-2 text-left">Convert</th>
            </tr>
            </thead>
            <tbody>
            @for(var lead : leads)
                <tr class="border-t hover:bg-gray-50">
                    <td class="px-4 py-2">${lead.getName()}</td>
                    <td class="px-4 py-2">${lead.getEmail()}</td>
                    <td class="px-4 py-2">${lead.getPhone()}</td>
                    <td class="px-4 py-2">${lead.getCompany()}</td>
                    <td class="p-3">
                        <span class="px-2 py-1 rounded text-sm ${
                        lead.getStatus().name().equals("QUALIFIED") ? "bg-green-100" :
                        lead.getStatus().name().equals("CONTACTED") ? "bg-blue-100" : "bg-gray-100"
                         }">${lead.getStatus()}</span>
                    </td>
                    <td class="px-4 py-2">
                        <a href="/jpa-leads/${lead.getId().toString()}/edit"
                           class="text-blue-600 hover:underline font-medium">
                            Редактировать
                        </a>
                    </td>
                    <td>
                        <form method="post" action="/jpa-leads/${lead.getId().toString()}/delete" class="inline"
                              onclick="return confirm('Удалить лида ${lead.getEmail()}?')">
                            <button type="submit" class="bg-red-600 text-white px-3 py-1 rounded hover:bg-red-700">
                                Удалить
                            </button>
                        </form>
                    </td>
                    <!-- Кнопка конвертации (только для QUALIFIED) -->
                    <td>
                        @if(lead.getStatus() == LeadStatus.QUALIFIED)
                            <a href="/jpa-deals/convert/${lead.getId().toString()}"
                               class="text-green-600 hover:underline font-medium">
                                Конвертировать
                            </a>
                        @endif
                    </td>
                </tr>
            @endfor
            </tbody>
        </table>
    </div>
`)